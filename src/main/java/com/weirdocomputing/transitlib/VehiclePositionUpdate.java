package com.weirdocomputing.transitlib;

import com.google.transit.realtime.GtfsRealtime;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * © 2020 Daniel Norton
 */
public class VehiclePositionUpdate {
    private final URL url;
    private List<VehiclePosition> vehiclePositions = new ArrayList<>();

    VehiclePositionUpdate(final String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    /**
     * fetch latest vehicle positions
     * @throws Exception
     */
    public List<VehiclePosition> fetch() throws Exception {
        CloseableHttpResponse response;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String stringBuffer;
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(
                this.url.openStream());
        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasVehicle()) {
                VehiclePosition vp = new VehiclePosition(entity.getVehicle());
                vehiclePositions.add(vp);
            } else if (entity.hasTripUpdate()) {
                System.err.printf("*** TripUpdate: %s\n", entity.getTripUpdate());
                throw new Exception("Unexpecte TripUpdate entity");
            } else {
                System.err.printf("*** ???: %s\n", entity.getAllFields());
                throw new Exception("Unrecognized entity");
            }
        }
        return vehiclePositions;
    }
}
