package com.weirdocomputing.transitlib;

import static java.lang.StrictMath.toIntExact;

/**
 * Values for GTFS static feed routes.txt "route_type" field
 */
public enum RouteType {
    STREET_RAIL(0),
    SUBWAY(1),
    RAIL(2),
    BUS(3),
    FERRY(4),
    CABLE_CAR(5),
    FUNICULAR(6),
    MAX_ROUTE_VALUE(6),
    UNRECOGNIZED(5730605087920018L); // never matches anything
    static final RouteType[] enumValues = {STREET_RAIL, SUBWAY, RAIL, BUS, FERRY, CABLE_CAR, FUNICULAR};

    private long value;

    RouteType(long value) {
        this.value = value;
    }

    public static RouteType fromLong(long longValue) {
        RouteType result;
        if (longValue >= 0 && longValue <= MAX_ROUTE_VALUE.value) {
            result = enumValues[toIntExact(longValue)];
        } else {
            result = UNRECOGNIZED;
            result.value = longValue;
        }
        return result;
    }

    public long getIndex() {
        return value;
    }
}
