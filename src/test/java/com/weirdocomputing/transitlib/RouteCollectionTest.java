package com.weirdocomputing.transitlib;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteCollectionTest {

    @Test
    void toJsonArray() {
        RouteCollection routeCollection = null;
        AgencyCollection agencyCollection = null;
        try {
            agencyCollection = new AgencyCollection("src/test/resources/agency.txt");
            routeCollection = new RouteCollection(agencyCollection, "src/test/resources/routes.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert agencyCollection != null;
        for (Route route: routeCollection.getAll().values()) {
            System.out.printf("Route: %s\n", route.toJsonObject().toString());
        }
    }
}