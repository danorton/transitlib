package com.weirdocomputing.transitlib;

import com.google.transit.realtime.GtfsRealtime;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

/**
 *
 * © 2020 Daniel Norton
 */
public class VehiclePositionCollection {
    private final URL url;
    private HashMap<String, VehiclePosition> vehiclePositions = new HashMap<>();
    private final Duration STALE_AGE = Duration.ofMinutes(1);

    VehiclePositionCollection(final String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    /**
     * Update latest vehicle positions
     * @return List of VehiclePosition records that have changed
     * @throws Exception If unable to fetch or if data fails validation
     */
    public HashMap<String, VehiclePosition> update() throws Exception {
        HashMap<String, VehiclePosition> newVehiclePositions = new HashMap<>();
        Instant staleTimestamp = Instant.now().plus(STALE_AGE);
        // read the positions from the server
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(this.url.openStream());

        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasVehicle()) {
                VehiclePosition newPosition = new VehiclePosition(entity.getVehicle());
                Instant timestamp = newPosition.getTimestamp();
                String vehicleId = newPosition.getVehicle().getId();
                VehiclePosition oldPosition = this.vehiclePositions.get(vehicleId);
                // ignore stale records
                if (timestamp.isBefore(staleTimestamp)) {
                    // Is the new position more recent than the current position?
                    if (oldPosition == null || (oldPosition.getTimestamp().compareTo(newPosition.getTimestamp()) < 0)) {
                        if (oldPosition == null) {
                            System.err.printf("[WARNING] New vehicle %s at time %s\n",
                                    vehicleId, timestamp.toString());
                        }
                        // update with the more recent position
                        this.vehiclePositions.put(vehicleId, newPosition);
                        // Add to hash of updated positions
                        newVehiclePositions.put(vehicleId, newPosition);
                    }
                } else {
                    // remove stale records
                    System.err.printf("[WARNING] Removing stale position for vehicle %s at time %s\n",
                        vehicleId, timestamp.toString());
                    vehiclePositions.remove(vehicleId);
                }
            } else if (entity.hasTripUpdate()) {
                System.err.printf("*** TripUpdate: %s\n", entity.getTripUpdate());
                throw new Exception("Unexpected TripUpdate entity");
            } else {
                System.err.printf("*** ???: %s\n", entity.getAllFields());
                throw new Exception("Unrecognized entity");
            }
        }

        return newVehiclePositions;
    }

    @NotNull
    public HashMap<String, VehiclePosition> getPositions() {
        return vehiclePositions;
    }

    int size() {
        return this.getPositions().size();
    }
}
