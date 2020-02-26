package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.transit.realtime.GtfsRealtime;
import org.jetbrains.annotations.NotNull;

/*
 * © 2020 Daniel Norton
 */

/**
 * GTFS realtime vehicle descriptor
 * Wrapper for the protobuf definition to provide JSON serialization
 */
public class VehicleDescriptor {
    private final GtfsRealtime.VehicleDescriptor gglVehicle;
    private transient static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    public VehicleDescriptor(GtfsRealtime.VehicleDescriptor vehicle) {
        this.gglVehicle = vehicle;
    }

    /**
     * Serialize to JSON object
     * @return JSON object
     */
    public JsonNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        if (this.gglVehicle.hasId()) {
            o.put("id", this.gglVehicle.getId());
        }
        if (this.gglVehicle.hasLabel()) {
            o.put("label", this.gglVehicle.getLabel());
        }
        if (this.gglVehicle.hasLicensePlate()) {
            o.put("licensePlate", this.gglVehicle.getLicensePlate());
        }
        return o;
    }

    @NotNull
    public String getId() {
        return this.gglVehicle.getId();
    }

}
