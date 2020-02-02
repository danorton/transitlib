package com.weirdocomputing.transitlib;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;


class VehiclePositionUpdateTest {

    @Test
    void fetch() {
        System.out.println("fetch() junit 5 test");
        VehiclePositionUpdate vehiclePositionUpdate =
                null;
        try {
            vehiclePositionUpdate = new VehiclePositionUpdate(
                "https://data.texas.gov/download/eiei-9rpf/application%2Foctet-stream");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            vehiclePositionUpdate.fetch();
        } catch (Exception e) {
            if (e.getMessage() == null) {
                e = new Exception(e.getCause());
            }
            e.printStackTrace();
        }
    }

}