package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opencsv.CSVReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * © 2020 Daniel Norton
 */
public class RouteCollection {
    @NotNull
    private HashMap <String,Route> routes = new HashMap<>();

    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    RouteCollection(final AgencyCollection agencies, final InputStream inputStream) throws IOException {
        final CSVReader reader = new CSVReader(new InputStreamReader(inputStream));

        String[] keys = null;
        for (String[] row : reader) {
            if (keys == null) {
                keys = row.clone();
            } else {
                Route route = new Route(agencies, keys, row);
                String routeId = route.getId();
                // validate that route_id is unique
                if (routes.containsKey(routeId)) {
                    throw new IllegalArgumentException("route_id must be unique");
                }
                routes.put(routeId, route);
            }
        }

        // We require at least one route
        if (routes.size() < 1) {
            throw new IllegalArgumentException("routes.txt must specify at least one route");
        }
    }

    @NotNull
    public ArrayNode toJsonArray() {
        ArrayNode a = jnf.arrayNode();
        routes.values().forEach( route -> a.add(route.toJsonObject()));
        return a;
    }

    @NotNull
    public HashMap<String, Route> getAll() {
        return routes;
    }


    public int size() {
        return routes.size();
    }

    @Nullable
    public Route get(final String id) {
        return routes.get(id);
    }

}
