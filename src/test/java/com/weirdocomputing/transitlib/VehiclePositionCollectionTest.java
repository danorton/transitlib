package com.weirdocomputing.transitlib;

import org.junit.jupiter.api.Test;

import java.io.IOException;


class VehiclePositionCollectionTest {

    @Test
    void fetch() {
        System.err.println("[WARNING] VehiclePosition.fetch(): fetching live, realtime data");
        VehiclePositionUpdate vehiclePositionUpdate = null;
        RouteCollection routeCollection;
        AgencyCollection agencyCollection;
        try {
            agencyCollection = new AgencyCollection("src/test/resources/agency.txt");
            routeCollection = new RouteCollection(agencyCollection, "src/test/resources/routes.txt");
            vehiclePositionUpdate = new VehiclePositionUpdate(
                "https://data.texas.gov/download/eiei-9rpf/application%2Foctet-stream");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (vehiclePositionUpdate != null) {
                for (VehiclePosition vp: vehiclePositionUpdate.fetch()) {
                    System.out.printf("VehiclePosition: %s\n", vp.toJsonObject().toString());
                }
            }
        } catch (Exception e) {
            Exception e1 = e;
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

}