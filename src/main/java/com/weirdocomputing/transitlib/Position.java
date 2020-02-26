package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.transit.realtime.GtfsRealtime;

/*
 * © 2020 Daniel Norton
 */

/**
 * Wrapper for GTFS realtime Position object to provide JSON serialization
 */
public class Position {
    private final GtfsRealtime.Position gglPos;

    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    public Position(GtfsRealtime.Position gglPos) {
        this.gglPos = gglPos;
    }

    /**
     * Serialize to JSON object
     * @return JSON object
     */
    public JsonNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        if (this.gglPos.hasLatitude()) {
            o.put("latitude", this.gglPos.getLatitude());
        }
        if (this.gglPos.hasLongitude()) {
            o.put("longitude", this.gglPos.getLongitude());
        }
        if (this.gglPos.hasBearing()) {
            o.put("bearing", this.gglPos.getBearing());
        }
        if (this.gglPos.hasOdometer()) {
            double odometer = this.gglPos.getOdometer();
            if (odometer > 0) {
                o.put("odometer", odometer);
            }
        }
        if (this.gglPos.hasSpeed()) {
            float speed = this.gglPos.getSpeed();
            if (speed > 0) {
                o.put("speed", speed);
            }
        }
        return o;
    }

}
