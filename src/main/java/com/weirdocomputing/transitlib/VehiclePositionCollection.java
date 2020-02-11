package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.transit.realtime.GtfsRealtime;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;

/**
 * A collection of real-time vehicle positions
 * © 2020 Daniel Norton
 */
public class VehiclePositionCollection {
    private final Logger logger = LoggerFactory.getLogger(VehiclePositionCollection.class);

    transient private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    /**
     * Ignore positions older than this
     */
    private final Duration staleAge;

    /**
     * current vehicle states
     */
    private final HashMap<String, VehiclePosition> vehiclePositions = new HashMap<>();

    public VehiclePositionCollection(Duration staleAge) {
        this.staleAge = staleAge;
    }

    public static ArrayNode toJsonArray(Collection<VehiclePosition> positions) {
        ArrayNode ar = jnf.arrayNode();
        for (VehiclePosition position: positions) {
            ar.add(position.toJsonObject());
        }
        return ar;
    }

    /**
     * Update latest vehicle positions
     * @return List of VehiclePosition records that have changed
     * FIXME - remove stale positions if we don't get an update
     * @throws Exception If unable to fetch or if data fails validation
     */
    public HashMap<String, VehiclePosition> update(InputStream inputStream) throws Exception {
        HashMap<String, VehiclePosition> newVehiclePositions = new HashMap<>();
        // read the positions
        GtfsRealtime.FeedMessage feed = null;
        feed = GtfsRealtime.FeedMessage.parseFrom(inputStream);
        if (feed == null) {
            return newVehiclePositions;
        }
        Instant staleTimestamp = Instant.now().minus(staleAge);
        flushStale(staleTimestamp);

        int positionCount = 0;
        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasVehicle()) {
                VehiclePosition newPosition = new VehiclePosition(entity.getVehicle());
                positionCount++;
                Instant timestamp = newPosition.getTimestamp();
                String vehicleId = newPosition.getVehicle().getId();
                VehiclePosition oldPosition = this.vehiclePositions.get(vehicleId);
                // ignore stale records
                if (timestamp.isAfter(staleTimestamp)) {
                    // Is the new position more recent than the current position?
                    if (oldPosition == null || (oldPosition.getTimestamp().compareTo(newPosition.getTimestamp()) < 0)) {
                        // update with the more recent position
                        this.vehiclePositions.put(vehicleId, newPosition);
                        // Add to hash of updated positions
                        newVehiclePositions.put(vehicleId, newPosition);
                    }
                }
            } else if (entity.hasTripUpdate()) {
                logger.error("TripUpdate: {}", entity.getTripUpdate());
                throw new Exception("Unexpected TripUpdate entity");
            } else {
                logger.error("???: {}", entity.getAllFields());
                throw new Exception("Unrecognized entity");
            }
        }
        return newVehiclePositions;
    }

    @NotNull
    public HashMap<String, VehiclePosition> getPositions() {
        return vehiclePositions;
    }

    public int size() {
        return this.getPositions().size();
    }

    public int flushStale(Instant staleTimestamp) {
        int flushCount = 0;
        if (staleTimestamp == null) {
            staleTimestamp = Instant.now().minus(staleAge);
        }
        for (String key: vehiclePositions.keySet()) {
            if (vehiclePositions.get(key).getTimestamp().isBefore(staleTimestamp)) {
                vehiclePositions.remove(key);
                flushCount++;
            }
        }
        return flushCount;
    }

    public void addPositions(HashMap<String, VehiclePosition> vehiclePositions) {
        this.vehiclePositions.putAll(vehiclePositions);
    }

    public ArrayNode toJsonArray() {
        return VehiclePositionCollection.toJsonArray(this.vehiclePositions.values());
    }
}
