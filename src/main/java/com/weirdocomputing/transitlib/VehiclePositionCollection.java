package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.transit.realtime.GtfsRealtime;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collection;
import java.util.List;

/**
 * A collection of real-time, timestamped vehicle positions
 * © 2020 Daniel Norton
 */
public class VehiclePositionCollection {
    private transient static final Logger logger = LoggerFactory.getLogger(VehiclePositionCollection.class);

    /**
     * GTFS realtime protobuf version (See gtfs-realtime.proto)
     */
    private transient static final String GTFS_REALTIME_VERSION = "1.0"; // See gtfs-realtime.proto

    /**
     * HashMap of VehiclePositions indexed by vehicle ID
     */
    @NotNull
    private HashMap<String, VehiclePosition> positionsHash;

    /**
     * Ignore positions older than this age
     */
    @NotNull
    private transient final Duration staleAge;

    /**
     * Create dummy null constructor without public access
     */
    @SuppressWarnings("unused")
    private VehiclePositionCollection() {
        //noinspection ConstantConditions
        this.staleAge = null;
        this.positionsHash = null;
    }

    /**
     * Construct empty collection
     * @param staleAge How long a position record is considered current
     */
    public VehiclePositionCollection(@NotNull Duration staleAge) {
        this.staleAge = staleAge;
        this.positionsHash = new HashMap<>();
    }

    /**
     * Copy constructor
     * @param vehiclePositionCollection collection to copy
     */
    public VehiclePositionCollection(@NotNull VehiclePositionCollection vehiclePositionCollection) {
        this(vehiclePositionCollection.staleAge,
             vehiclePositionCollection.positionsHash.values().toArray(new VehiclePosition[0]));
    }

    /**
     * Construct collection from array of positions
     * @param staleAge How long a position record is considered current
     * @param positions positions to initialize
     */
    public VehiclePositionCollection(@NotNull Duration staleAge, @NotNull VehiclePosition[] positions) {
        this.staleAge = staleAge;
        positionsHash = new HashMap<>();
        this.putAll(positions);
    }

    /**
     * Update the collection with entries from a Gtfs feed input stream
     * @param inputStream An input stream that contains a Gtfs feed
     * @return Collection of records added
     * @throws Exception If unable to fetch or if data fails validation
     */
    public VehiclePositionCollection update(InputStream inputStream) throws Exception {
        return update(GtfsRealtime.FeedMessage.parseFrom(inputStream));
    }

    /**
     * Update latest vehicle positions from feed message
     * @param feedMessage feed message containing VehiclePosition records
     * @return List of VehiclePosition records that have changed
     * FIXME - remove stale positions if we don't get an update
     * @throws Exception If unable to fetch or if data fails validation
     */
    public VehiclePositionCollection update(GtfsRealtime.FeedMessage feedMessage) throws Exception {
        logger.debug("Incoming serialized feed message size: {}", feedMessage.getSerializedSize());
        ArrayList<VehiclePosition> newVehiclePositions = new ArrayList<>();

        for (GtfsRealtime.FeedEntity entity : feedMessage.getEntityList()) {
            if (entity.hasVehicle()) {
                newVehiclePositions.add(new VehiclePosition(entity.getVehicle()));
            } else if (entity.hasTripUpdate()) {
                logger.error("TripUpdate: {}", entity.getTripUpdate());
                throw new Exception("Unexpected TripUpdate entity");
            } else {
                logger.error("???: {}", entity.getAllFields());
                throw new Exception("Unrecognized entity");
            }
        }
        this.putAll(newVehiclePositions.toArray(new VehiclePosition[0]));
        return new VehiclePositionCollection(staleAge, newVehiclePositions.toArray(new VehiclePosition[0]));
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
     * Add the given VehiclePosition to the Collection if it's new information
     * @param vehiclePosition The position to add
     * @return true if the collection changed
     */
    public boolean put(VehiclePosition vehiclePosition) {
        return put(vehiclePosition, Instant.now().minus(staleAge));
    }

    /**
     * Add the given VehiclePosition to the Collection if it's new information
     * @param vehiclePosition The position to add
     * @param staleTimestamp Positions timestamped before this are discarded
     * @return true if the collection changed
     */
    private  boolean put(VehiclePosition vehiclePosition, @NotNull Instant staleTimestamp) {
        boolean changed = false;
        String vehicleId = vehiclePosition.getVehicle().getId();
        // ignore if stale
        if (!vehiclePosition.isOlderThan(staleTimestamp)) {
            if (this.positionsHash.containsKey(vehicleId)) {
                if (vehiclePosition.getTimestamp().isAfter(this.positionsHash.get(vehicleId).getTimestamp())) {
                    // More recent position for a vehicle already in our collection
                    changed = true;
                }
            } else {
                // new vehicle to our collection
                changed = true;
            }
        }
        if (changed) {
            this.positionsHash.put(vehicleId, vehiclePosition);
        }
        return changed;
    }

    /**
     * Add given VehiclePosition records to the collection
     * @param vehiclePositions positions to add
     */
    public void putAll(VehiclePosition[] vehiclePositions) {
        Instant staleTimestamp = Instant.now().minus(staleAge);
        for (VehiclePosition p: vehiclePositions) {
            this.put(p, staleTimestamp);
        }
    }

    /**
     * Remove the specified entry from the collection
     * @param vehicleId vehicle ID
     * @return VehiclePosition or null, if no such entry
     */
    public VehiclePosition remove(String vehicleId) {
        VehiclePosition result = null;
        if (this.positionsHash.containsKey(vehicleId)) {
            result = this.remove(vehicleId);
            this.positionsHash.remove(vehicleId);
        }
        return result;
    }

    /**
     * Get collection size
     * @return number of entries in collection
     */
    public int size() {
        return positionsHash.size();
    }

    /**
     * Remove all entries from the collection
     */
    public void clear() {
        this.positionsHash.clear();
    }

    public String[] keys() {
        return positionsHash.keySet().toArray(new String[0]);
    }

    public Set<String> keySet() {
        return positionsHash.keySet();
    }

    public Collection<VehiclePosition> values() {
        return this.positionsHash.values();
    }

    /**
     * Purge stale records from the collection
     * @return the number of records purged
     */
    @SuppressWarnings("UnusedReturnValue")
    public int purgeStale() {
        Instant staleTime = Instant.now().minus(staleAge);
        int purgeCount = 0;
        for (String key: this.positionsHash.keySet()) {
            if (this.positionsHash.get(key).isOlderThan(staleTime)) {
                this.remove(key);
                purgeCount++;
            }
        }
        return purgeCount;
    }

    /**
     * Get serialized collection as a JSON node
     * @return JSON node
     */
    @SuppressWarnings("unused")
    public JsonNode toJsonArray() {
        ArrayNode ar = JsonNodeFactory.instance.arrayNode();
        for (VehiclePosition position: this.positionsHash.values()) {
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

        List<GtfsRealtime.FeedEntity> vehicleEntities = new ArrayList<>();
        for (VehiclePosition vp: this.positionsHash.values()) {
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

        logger.debug("Serialized feed count {} size {}",
                feedMessage.getEntityCount(), feedMessage.getSerializedSize());
        return feedMessage;
    }

}
