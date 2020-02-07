package com.weirdocomputing.transitlib;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;


class VehiclePositionCollectionTest {

    @Test
    void fetch() {
        System.err.println("[WARNING] VehiclePosition.fetch(): fetching live, realtime data");
        VehiclePositionCollection positionCollection = null;
        RouteCollection routeCollection;
        AgencyCollection agencyCollection;
        try {
            agencyCollection = new AgencyCollection("src/test/resources/agency.txt");
            routeCollection = new RouteCollection(agencyCollection, "src/test/resources/routes.txt");
            positionCollection = new VehiclePositionCollection(
                "https://data.texas.gov/download/eiei-9rpf/application%2Foctet-stream");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (positionCollection != null) {
                // fetch initial values
                Map<String,VehiclePosition> newPositions = positionCollection.update();
                System.out.printf("[INFO] Initial values: %d positions\n", newPositions.size());
//                for (VehiclePosition vp: newPositions.values()) {
//                    System.out.printf("VehiclePosition: %s\n", vp.toJsonObject().toString());
//                }
                int totalWait = 0;
                while (totalWait < 30) {
                    // pause
                    int delaySeconds = newPositions.size() == 0 ? 1 : 10;
                    System.out.printf("[INFO] %d: Sleeping %d secs\n",totalWait, delaySeconds);
                    Thread.sleep(delaySeconds*1000);
                    totalWait += delaySeconds;
                    // fetch changed values
                    newPositions = positionCollection.update();
                    System.out.printf("[INFO] %d updated of %d total\n",
                            newPositions.size(), positionCollection.size());
//                    for (VehiclePosition vp: newPositions.values()) {
//                        System.out.printf("VehiclePosition: %s\n", vp.toJsonObject().toString());
//                    }
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