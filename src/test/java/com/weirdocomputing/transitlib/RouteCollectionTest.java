package com.weirdocomputing.transitlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

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
        S3Client s3Client = S3Client.builder().build();
        InputStream s3AgenciesStream;
        AgencyCollection agencyCollection;
        InputStream s3RoutesStream;
        RouteCollection routeCollection = null;
        GetObjectRequest.Builder s3BucketBuilder = GetObjectRequest.builder().bucket(S3_BUCKET_NAME);
        s3AgenciesStream = s3Client.getObject(s3BucketBuilder.key(S3_AGENCIES_KEY).build());
        s3RoutesStream = s3Client.getObject(s3BucketBuilder.key(S3_ROUTES_KEY).build());
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