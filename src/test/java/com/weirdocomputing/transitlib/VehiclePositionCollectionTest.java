package com.weirdocomputing.transitlib;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.transit.realtime.GtfsRealtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


class VehiclePositionCollectionTest {
    private final Logger logger = LoggerFactory.getLogger(VehiclePositionCollectionTest.class);

    private static final String PUBLISHER_ID = "capmetro";
    private static final String S3_BUCKET_NAME = String.format("weirdocomputing.transit.%s", PUBLISHER_ID);
    private static final String S3_AGENCIES_KEY = "gtfs/static/agency.txt";
    private static final String S3_ROUTES_KEY = "gtfs/static/routes.txt";

    private static final String VEHICLE_POSITIONS_URL =
        "https://data.texas.gov/download/eiei-9rpf/application%2Foctet-stream";
    private static final Duration STALE_AGE = Duration.ofMinutes(60);

    @Test
    void update() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        InputStream s3AgenciesStream = null;
        InputStream s3RoutesStream = null;
        HttpURLConnection urlConnection = getHttpURLConnection();
        String etag = urlConnection.getHeaderField("ETag");
        logger.info("first ETag: \"{}\"", etag);
        logger.warn("VehiclePosition.fetch(): fetching live, realtime data");
        VehiclePositionCollection positionCollection = null;
        RouteCollection routeCollection;
        AgencyCollection agencyCollection;

        try {
            s3AgenciesStream = s3Client.getObject(S3_BUCKET_NAME, S3_AGENCIES_KEY).getObjectContent();
            s3RoutesStream = s3Client.getObject(S3_BUCKET_NAME, S3_ROUTES_KEY).getObjectContent();
            agencyCollection = new AgencyCollection(s3AgenciesStream);
            routeCollection = new RouteCollection(agencyCollection, s3RoutesStream);
            positionCollection = VehiclePositionCollection.fromInputStream(STALE_AGE, urlConnection.getInputStream());
            urlConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (s3AgenciesStream != null) {
                    s3AgenciesStream.close();
                }
                if (s3RoutesStream != null) {
                    s3RoutesStream.close();
                }
                routeCollection = null;
                agencyCollection = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (positionCollection != null) {
                Collection<VehiclePosition> newPositions = positionCollection.values();
                logger.info("newPositions.length {} positionCollection.size() {}",
                        newPositions.size(), positionCollection.size());
                // fetch initial values

                int totalWait = 0;
                while (totalWait < 60) {
                    // pause
                    int delaySeconds = newPositions.size() == 0 ? 1 : 10;
                    logger.info("{}: Sleeping {} secs",totalWait, delaySeconds);
                    Thread.sleep(delaySeconds*1000);
                    totalWait += delaySeconds;
                    // fetch changed values
                    urlConnection = getHttpURLConnection();
                    urlConnection.setRequestProperty("ETag", etag);
                    String newEtag = urlConnection.getHeaderField("ETag");
                    logger.info("new ETag: \"{}\"", newEtag);
                    if (!newEtag.equals(etag)) {
                        // update the collection from the stream
                        newPositions = positionCollection.update(urlConnection.getInputStream()).values();
                        urlConnection.disconnect();
                        logger.info("newPositions.length {} positionCollection.size() {}",
                                newPositions.size(), positionCollection.size());
                        // serialize and deserialize...
                        GtfsRealtime.FeedMessage feedMessage = positionCollection.toFeedMessage(false);
                        logger.info("BEFORE serialize/de-serialize: recs {} bytes {}",
                                positionCollection.size(), feedMessage.getSerializedSize());
                        positionCollection.update(positionCollection.toFeedMessage(false));
                        feedMessage = positionCollection.toFeedMessage(false);
                        logger.info(" AFTER serialize/de-serialize: recs {} bytes {}",
                                positionCollection.size(), feedMessage.getSerializedSize());
                        etag = newEtag;
                        logger.info("{} updated of {} total", newPositions.size(), positionCollection.size());
                    } else {
                        newPositions = new ArrayList<>();
                        logger.warn("ETag unchanged; skipping...");
                    }
                }
            }
        } catch (Exception e) {
            while (e.getMessage() == null) {
                Exception cause = new Exception(e.getCause());
                if (cause.getMessage() == null) {
                    break;
                }
                e = cause;
            }
            e.printStackTrace();
        }
    }

    private HttpURLConnection getHttpURLConnection() {
        HttpURLConnection urlConnection = null;
        URL url = null;

        try {
            url = new URL(VEHICLE_POSITIONS_URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert url != null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert urlConnection != null;
        urlConnection.setUseCaches(true);

        return urlConnection;
    }

}