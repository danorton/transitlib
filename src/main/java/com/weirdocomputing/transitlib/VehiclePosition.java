package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.transit.realtime.GtfsRealtime;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Real-time vehicle position
 * © 2020 Daniel Norton
 */
public class VehiclePosition {

    /**
     * Raw Google VehiclePosition object
     */
    @NotNull
    private final GtfsRealtime.VehiclePosition gglVP;

    transient private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    VehiclePosition(@NotNull GtfsRealtime.VehiclePosition gglVP) {
        this.gglVP = gglVP;
    }


    @NotNull
    public ObjectNode toJsonObject() {
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

}
