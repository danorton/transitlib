package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/*
 * © 2020 Daniel Norton
 */

/**
 * Wrapper for GTFS realtime vehicle position to provide JSON serialization
 */
public class VehiclePosition {
    transient private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    /**
     * Raw Google VehiclePosition object
     */
    @NotNull
    private final GtfsRealtime.VehiclePosition gglVP;


    VehiclePosition(@NotNull GtfsRealtime.VehiclePosition gglVP) {
        this.gglVP = gglVP;
        try {
            GtfsRealtime.VehiclePosition.parseFrom(gglVP.toByteArray());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Serialize to JSON object
     * @return JSON object
     */
    @NotNull
    public JsonNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        if (this.gglVP.hasPosition()) {
            o.putPOJO("position", this.getPosition().toJsonObject());
        }
        if (this.gglVP.hasTrip()) {
            o.putPOJO("trip", (new TripDescriptor(this.gglVP.getTrip())).toJsonObject());
        }
        o.putPOJO("vehicle", (new VehicleDescriptor(this.gglVP.getVehicle())).toJsonObject());
        if (this.gglVP.hasCongestionLevel()) {
            o.put("congestionLevel", this.gglVP.getCongestionLevel().getNumber());
        }
        if (this.gglVP.hasCurrentStatus()) {
            o.put("stopStatus", this.gglVP.getCurrentStatus().getNumber());
        }
        if (this.gglVP.hasCurrentStopSequence()) {
            o.put("stopSequence", this.gglVP.getCurrentStopSequence());
        }
        if (this.gglVP.hasOccupancyStatus()) {
            o.put("occupancyStatus", this.gglVP.getOccupancyStatus().getNumber());
        }
        if (this.gglVP.hasStopId()) {
            o.put("stopId", this.gglVP.getStopId());
        }
        o.put("timestamp", this.gglVP.getTimestamp());
        return o;
    }

    public GtfsRealtime.VehiclePosition getGoogleVehiclePosition() {
        return gglVP;
    }

    @NotNull
    public Position getPosition() {
        return new Position(this.gglVP.getPosition());
    }

    @NotNull
    public VehicleDescriptor getVehicle() {
        return new VehicleDescriptor(this.gglVP.getVehicle());
    }

    @NotNull
    public Instant getTimestamp() {
        return Instant.ofEpochSecond(this.gglVP.getTimestamp());
    }

    public int getSerializedSize() {
        return this.gglVP.getSerializedSize();
    }

    @NotNull
    public byte[] toByteArray() {
        return this.gglVP.toByteArray();
    }

    public boolean isOlderThan(Instant staleAge) {
        return this.getTimestamp().isBefore(staleAge);
    }


    transient private String hashKey = null;
    /**
     * Get a key to uniquely identify a VehiclePosition instance.
     * The key is constructed from the vehicle ID and the timestamp.
     * @return unique hash string
     */
    @NotNull
    public String getHashString() {
        if (this.hashKey == null) {
            this.hashKey = String.format("%08x",(
                    Integer.toHexString(this.getVehicle().getId().hashCode())
                  + Integer.toHexString(String.valueOf(this.getTimestamp().toEpochMilli()).hashCode()))
                .hashCode());
        }
        return this.hashKey;
    }

}
