package com.weirdocomputing.transitlib;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class RouteCollectionTest {
    private final Logger logger = LoggerFactory.getLogger(RouteCollectionTest.class);

    private static final String PUBLISHER_ID = "capmetro";
    private static final String S3_BUCKET_NAME = String.format("weirdocomputing.transit.%s", PUBLISHER_ID);
    private static final String S3_AGENCIES_KEY = "gtfs/static/agency.txt";
    private static final String S3_ROUTES_KEY = "gtfs/static/routes.txt";

    @Test
    void toJsonArray() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        InputStream s3AgenciesStream;
        AgencyCollection agencyCollection;
        InputStream s3RoutesStream;
        RouteCollection routeCollection = null;
        s3AgenciesStream = s3Client.getObject(S3_BUCKET_NAME, S3_AGENCIES_KEY).getObjectContent();
        s3RoutesStream = s3Client.getObject(S3_BUCKET_NAME, S3_ROUTES_KEY).getObjectContent();
        try {
            agencyCollection = new AgencyCollection(s3AgenciesStream);
            routeCollection = new RouteCollection(agencyCollection, s3RoutesStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                s3AgenciesStream.close();
                s3RoutesStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assert routeCollection != null;
        for (Route route: routeCollection.getAll().values()) {
            logger.info("Route: {}", route.toJsonObject().toString());
        }
    }
}