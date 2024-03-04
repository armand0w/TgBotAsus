package com.armandow.devices.utils;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"java:S5786"})
public class DevicesUtilsTest {

    @Test
    void testGetRelease() {
        assertNull(null, DevicesUtils.getRelease());
    }

    @Test
    void testDateParse() throws ParseException {
        var dateString = "2023-02-03 15:55:32";
        var dateDate = new Date(1675461332000L);

        var dateParsed = DevicesUtils.parseSQLDateTime(dateString);
        assertEquals(dateDate, dateParsed);

        var otherDate = DevicesUtils.parseToSQLFormat(dateDate);
        assertEquals(dateString, otherDate);
    }

    @Test
    void testIsAuthorized() {
        assertFalse(DevicesUtils.isAuthorized(new JSONObject().put("id", 0L)));
        assertTrue(DevicesUtils.isAuthorized(new JSONObject().put("id", DevicesUtils.getConfig().bot().userId())));
    }
}
