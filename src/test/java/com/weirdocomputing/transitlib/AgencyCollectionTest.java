package com.weirdocomputing.transitlib;

import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AgencyCollectionTest {


    @Test
    void toJsonArray() {
        AgencyCollection agencyCollection = null;
        try {
            agencyCollection = new AgencyCollection("src/test/resources/agency.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Agency agency: agencyCollection.getAll().values()) {
            System.out.printf("Agency: %s\n", agency.toJsonObject().toString());
        }
    }
}