package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.transit.realtime.GtfsRealtime;

/*
 * © 2020 Daniel Norton
 */

/**
 * Wrapper for GTFS realtime trip descriptor to provide JSON serialization
 */
public class TripDescriptor {
    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    private final GtfsRealtime.TripDescriptor gglTD;

    public TripDescriptor(GtfsRealtime.TripDescriptor gglTD) {
        this.gglTD = gglTD;
    }

    /**
     * Serialize to JSON object
     * @return JSON object
     */
    public JsonNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        if (this.gglTD.hasDirectionId()) {
            o.put("directionId", this.gglTD.getDirectionId());
        }
        if (this.gglTD.hasRouteId()) {
            o.put("routeId", this.gglTD.getRouteId());
        }
        if (this.gglTD.hasScheduleRelationship()) {
            o.put("scheduleRelationship", gglTD.getScheduleRelationship().getNumber());
        }
        if (this.gglTD.hasStartDate()) {
            o.put("startDate", gglTD.getStartDate());
        }
        if (this.gglTD.hasStartTime()) {
            o.put("startTime", gglTD.getStartTime());
        }
        if (this.gglTD.hasTripId()) {
            o.put("tripId", gglTD.getTripId());
        }
        return o;
    }

    public GtfsRealtime.TripDescriptor getGglTD() {
        return gglTD;
    }
}
