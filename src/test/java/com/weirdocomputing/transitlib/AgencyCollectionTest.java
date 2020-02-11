package com.weirdocomputing.transitlib;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;


import java.io.InputStream;

class AgencyCollectionTest {
    private final Logger logger = LoggerFactory.getLogger(AgencyCollectionTest.class);

    private static final String PUBLISHER_ID = "capmetro";
    private static final String S3_BUCKET_NAME = String.format("weirdocomputing.transit.%s", PUBLISHER_ID);
    private static final String S3_AGENCIES_KEY = "gtfs/static/agency.txt";

    @Test
    void toJsonArray() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        InputStream s3AgenciesStream;
        AgencyCollection agencyCollection = null;
        s3AgenciesStream = s3Client.getObject(S3_BUCKET_NAME, S3_AGENCIES_KEY).getObjectContent();
        try {
            agencyCollection = new AgencyCollection(s3AgenciesStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert agencyCollection != null;
        for(Agency agency: agencyCollection.getAll().values()) {
            logger.info("Agency: {}", agency.toJsonObject().toString());
        }
    }
}