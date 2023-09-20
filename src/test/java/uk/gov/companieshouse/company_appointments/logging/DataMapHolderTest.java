package uk.gov.companieshouse.company_appointments.logging;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.logging.util.DataMap;

class DataMapHolderTest {

    @BeforeEach
    void setUp() {
        DataMapHolder.clear();
    }

    @Test
    void getLogMapWithExplicitRequestId() {
        DataMapHolder.initialise("requestId");

        var logMap = DataMapHolder.getLogMap();
        assertEquals("requestId", DataMapHolder.getRequestId());
    }

    @Test
    void getLogMapWithDefaultRequestId() {
        var logMap = DataMapHolder.getLogMap();
        assertEquals("uninitialised", DataMapHolder.getRequestId());
    }

    @Test
    void get() {
        DataMapHolder.initialise("requestId");

        DataMap.Builder builder = DataMapHolder.get();
        DataMap dataMap = builder.build();
        assertEquals("requestId", dataMap.getLogMap().get("request_id"));
    }

    @Test
    void clear() {
        DataMapHolder.clear();
        assertEquals("uninitialised", DataMapHolder.getRequestId());

        var logMap = DataMapHolder.getLogMap();
        assertTrue(logMap.containsKey("request_id"));
    }
}