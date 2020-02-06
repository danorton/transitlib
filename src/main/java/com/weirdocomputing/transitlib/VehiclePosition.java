package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.transit.realtime.GtfsRealtime;

/**
 * Real-time vehicle position
 * © 2020 Daniel Norton
 */
public class VehiclePosition {
    private final GtfsRealtime.VehiclePosition gglVP;
    VehiclePosition(GtfsRealtime.VehiclePosition gglVP) {
        this.gglVP = gglVP;
    }

    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    public ObjectNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        if (this.gglVP.hasPosition()) {
            o.putPOJO("position", this.getPosition().toJsonObject());
        }
        if (this.gglVP.hasTrip()) {
            o.putPOJO("trip", (new TripDescriptor(this.gglVP.getTrip())).toJsonObject());
        }
        if (this.gglVP.hasVehicle()) {
            o.putPOJO("vehicle", (new VehicleDescriptor(this.gglVP.getVehicle())).toJsonObject());
        }
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
        if (this.gglVP.hasTimestamp()) {
            o.put("timestamp", this.gglVP.getTimestamp());
        }
        return o;
    }

    public final Position getPosition() {
        return new Position(this.gglVP.getPosition());
    }

}
