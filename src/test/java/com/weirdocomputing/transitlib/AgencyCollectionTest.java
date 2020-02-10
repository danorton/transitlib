package com.weirdocomputing.transitlib;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AgencyCollectionTest {
    private final Logger logger = LoggerFactory.getLogger(AgencyCollectionTest.class);

    private static final String PUBLISHER_ID = "capmetro";
    private static final String S3_BUCKET_NAME = String.format("weirdocomputing.transit.%s", PUBLISHER_ID);
    private static final String S3_AGENCIES_KEY = "gtfs/static/agency.txt";

    @Test
    void toJsonArray() {
        S3Client s3Client = S3Client.builder().build();
        InputStream s3AgenciesStream = null;
        AgencyCollection agencyCollection = null;
        GetObjectRequest.Builder s3BucketBuilder = GetObjectRequest.builder().bucket(S3_BUCKET_NAME);
        s3AgenciesStream = s3Client.getObject(s3BucketBuilder.key(S3_AGENCIES_KEY).build());
        try {
            agencyCollection = new AgencyCollection(s3AgenciesStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Agency agency: agencyCollection.getAll().values()) {
            logger.info("Agency: {}", agency.toJsonObject().toString());
        }
    }
}