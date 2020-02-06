package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opencsv.CSVReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * © 2020 Daniel Norton
 */
public class AgencyCollection {
    @NotNull
    private HashMap <String,Agency> agencies = new HashMap<>();
    @Nullable
    private Agency defaultAgency;

    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    AgencyCollection(final String fileSpec) throws IOException {
        final CSVReader reader = new CSVReader(new FileReader(fileSpec));
        String[] keys = null;
        for (String[] row : reader) {
            if (keys == null) {
                keys = row.clone();
            } else {
                Agency agency = new Agency(keys, row);
                String agencyId = agency.getId();
                // validate that agency_id is unique
                if (agencies.containsKey(agencyId)) {
                    throw new IllegalArgumentException("agency_id must be unique");
                }
                agencies.put(agencyId, agency);
                if (this.defaultAgency == null) {
                    this.defaultAgency = agency;
                }
            }
        }

        // We require at least one agency
        if (defaultAgency == null) {
            throw new IllegalArgumentException("agency.txt must specify at least one agency");
        }
    }

    @NotNull
    public ArrayNode toJsonArray() {
        ArrayNode a = jnf.arrayNode();
        agencies.values().forEach( agency -> a.add(agency.toJsonObject()));
        return a;
    }

    @NotNull
    public HashMap<String, Agency> getAll() {
        return agencies;
    }

    public int size() {
        return agencies.size();
    }

    @Nullable
    public Agency get(final String id) {
        return agencies.get(id);
    }

    @NotNull
    public Agency getDefaultAgency() {
        assert defaultAgency != null;
        return defaultAgency;
    }
}
