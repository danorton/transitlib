package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.transit.realtime.GtfsRealtime;

/**
 * © 2020 Daniel Norton
 */
public class TripDescriptor {
    private final GtfsRealtime.TripDescriptor gglTD;

    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    public TripDescriptor(GtfsRealtime.TripDescriptor gglTD) {
        this.gglTD = gglTD;
    }

    public ObjectNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        if (this.gglTD.hasDirectionId()) {
            o.put("direction_id", this.gglTD.getDirectionId());
        }
        if (this.gglTD.hasRouteId()) {
            o.put("route_id", this.gglTD.getRouteId());
        }
        if (this.gglTD.hasScheduleRelationship()) {
            o.put("sched_rel", gglTD.getScheduleRelationship().getNumber());
        }
        if (this.gglTD.hasDirectionId()) {
            o.put("dir_id", gglTD.getDirectionId());
        }
        if (this.gglTD.hasStartDate()) {
            o.put("start_date", gglTD.getStartDate());
        }
        if (this.gglTD.hasStartTime()) {
            o.put("start_time", gglTD.hasStartTime());
        }
        if (this.gglTD.hasTripId()) {
            o.put("trip_id", gglTD.getTripId());
        }
        return o;
    }

    public GtfsRealtime.TripDescriptor getGglTD() {
        return gglTD;
    }
}
