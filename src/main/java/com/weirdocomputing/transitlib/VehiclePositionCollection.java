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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A collection of real-time vehicle positions
 * © 2020 Daniel Norton
 */
public class VehiclePositionCollection {
    private static final Logger logger = LoggerFactory.getLogger(VehiclePositionCollection.class);
    transient private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    /**
     * GTFS realtime protobuf version (See gtfs-realtime.proto)
     */
    private static final String GTFS_REALTIME_VERSION = "1.0"; // See gtfs-realtime.proto

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

    public HashMap<String, VehiclePosition> update(InputStream inputStream) throws Exception {
        return update(GtfsRealtime.FeedMessage.parseFrom(inputStream));
    }

        /**
         * Update latest vehicle positions from feed message
         * @param feedMessage feed message containing VehiclePosition records
         * @return List of VehiclePosition records that have changed
         * FIXME - remove stale positions if we don't get an update
         * @throws Exception If unable to fetch or if data fails validation
         */
    public HashMap<String, VehiclePosition> update(GtfsRealtime.FeedMessage feedMessage) throws Exception {
        logger.debug("Incoming serialized feed message size: {}", feedMessage.getSerializedSize());
        HashMap<String, VehiclePosition> newVehiclePositions = new HashMap<>();
        if (feedMessage == null) {
            return newVehiclePositions;
        }
        Instant staleTimestamp = Instant.now().minus(staleAge);
        purgeStale(staleTimestamp);

        for (GtfsRealtime.FeedEntity entity : feedMessage.getEntityList()) {
            if (entity.hasVehicle()) {
                VehiclePosition newPosition = new VehiclePosition(entity.getVehicle());
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

    /**
     * Build instance from an input stream
     * @param staleAge age of records that can be purged
     * @param inputStream Input stream containing GTFS realtime feed
     * @return instance of VehiclePositionCollection
     * @throws Exception If unable to fetch or if data fails validation
     */
    public static VehiclePositionCollection fromInputStream(
            Duration staleAge,
            InputStream inputStream) throws Exception {
        VehiclePositionCollection collection = new VehiclePositionCollection(staleAge);
        collection.update(inputStream);
        return collection;
    }

    /**
     * @return HashMap of our collection, indexed by Vehicle ID
     */
    @NotNull
    public HashMap<String, VehiclePosition> getPositions() {
        return vehiclePositions;
    }

    /**
     * @return the number of positions in our collection
     */
    public int size() {
        return this.getPositions().size();
    }

    /**
     * Purge stale records from the collection
     * @param staleTimestamp Purge collection of records with timestamps before this time
     * @return the number of records purged
     */
    @SuppressWarnings("UnusedReturnValue")
    public int purgeStale(Instant staleTimestamp) {
        int purgeCount = 0;
        if (staleTimestamp == null) {
            staleTimestamp = Instant.now().minus(staleAge);
        }
        for (String key: vehiclePositions.keySet()) {
            if (vehiclePositions.get(key).getTimestamp().isBefore(staleTimestamp)) {
                vehiclePositions.remove(key);
                purgeCount++;
            }
        }
        return purgeCount;
    }

    /**
     * Add given positions to our collection
     * @param vehiclePositions positions to add
     */
    @SuppressWarnings("unused")
    public void addPositions(HashMap<String, VehiclePosition> vehiclePositions) {
        this.vehiclePositions.putAll(vehiclePositions);
    }

    /**
     * Get serialized collection as a JSON array
     * @return JSON array
     */
    @SuppressWarnings("unused")
    public ArrayNode toJsonArray() {
        ArrayNode ar = jnf.arrayNode();
        for (VehiclePosition position: this.vehiclePositions.values()) {
            ar.add(position.toJsonObject());
        }
        return ar;
    }

    /**
     * Get serialized collection as a protobuf feed message
     * @param isDifferential if this collection is not all vehicles, but only
     *                       recently changed vehicles
     * @return GtfsRealtime.FeedMessage that represents this collection
     */
    @SuppressWarnings("unused")
    public GtfsRealtime.FeedMessage toFeedMessage(boolean isDifferential) {
        GtfsRealtime.FeedHeader feedHeader = GtfsRealtime.FeedHeader.newBuilder()
            .setGtfsRealtimeVersion(GTFS_REALTIME_VERSION)
            .setIncrementality(isDifferential
                ? GtfsRealtime.FeedHeader.Incrementality.DIFFERENTIAL
                : GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET)
            .setTimestamp(Instant.now().toEpochMilli())
            .build();

        ArrayList<GtfsRealtime.FeedEntity> vehicleEntities = new ArrayList<>();
        for (VehiclePosition vp: this.vehiclePositions.values()) {
            GtfsRealtime.VehiclePosition gglVP = vp.getGoogleVehiclePosition();
            vehicleEntities.add(GtfsRealtime.FeedEntity.newBuilder()
                .setId(gglVP.getTimestamp() + ":" + gglVP.getVehicle().getId())
                .setVehicle(vp.getGoogleVehiclePosition())
                .build());
        }
        GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.newBuilder()
            .setHeader(feedHeader)
            .addAllEntity(vehicleEntities)
            .build();
        logger.debug("Serialized feed message size: {}", feedMessage.getSerializedSize());
        return feedMessage;
    }
}
