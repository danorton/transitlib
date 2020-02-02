package com.weirdocomputing.transitlib;

import com.google.transit.realtime.GtfsRealtime;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 * © 2020 Daniel Norton
 */
public class VehiclePositionUpdate {
    private final URL url;
    List<VehiclePosition> vehiclePositions;

    VehiclePositionUpdate(final String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    /**
     * fetch latest vehicle positions
     * @throws Exception
     */
    public void fetch() throws Exception {
        CloseableHttpResponse response;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String stringBuffer;
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(
                this.url.openStream());
        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            System.out.println(entity.getAllFields());
            if (entity.hasTripUpdate()) {
                System.out.println(entity.getTripUpdate());
            }
        }
    }
}
