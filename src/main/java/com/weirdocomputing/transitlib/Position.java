package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.transit.realtime.GtfsRealtime;

/**
 * A geo position
 * © 2020 Daniel Norton
 */
public class Position {
    private final GtfsRealtime.Position gglPos;

    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    public Position(GtfsRealtime.Position gglPos) {
        this.gglPos = gglPos;
    }

    /**
     * @return JSON string
     */
    public String toJson() {
        return toJson(false);
    }


    /**
     * @param makePretty Include indentation an newlines, else all on a single line
     * @return
     */
    public String toJson(boolean makePretty) {
        return this.toJsonObject().toString();
    }

    public ObjectNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        if (this.gglPos.hasLatitude()) {
            o.put("lat", this.gglPos.getLatitude());
        }
        if (this.gglPos.hasLongitude()) {
            o.put("lon", this.gglPos.getLongitude());
        }
        if (this.gglPos.hasBearing()) {
            o.put("brng", this.gglPos.getBearing());
        }
        if (this.gglPos.hasOdometer()) {
            double odometer = this.gglPos.getOdometer();
            if (odometer > 0) {
                o.put("odo", odometer);
            }
        }
        if (this.gglPos.hasSpeed()) {
            float speed = this.gglPos.getSpeed();
            if (speed > 0) {
                o.put("spd", speed);
            }
        }
        return o;
    }

}
