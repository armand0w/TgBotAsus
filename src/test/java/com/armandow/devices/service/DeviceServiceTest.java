package com.armandow.devices.service;

import com.armandow.devices.utils.DevicesUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({"java:S5786"})
public class DeviceServiceTest {

    @Test
    @Order(1)
    void testGetAllDevices() throws Exception {
        var devices = DeviceService.getAllDevices();
        assertNotNull(devices);

        var devicesList = devices.getJSONArray("data");
        for ( var device: devicesList ) {
            log.debug( DevicesUtils.getNotEmptyName((JSONObject) device) );
            assertFalse(DevicesUtils.getNotEmptyName((JSONObject) device).contains("null"));
            assertFalse(DevicesUtils.getNotEmptyName((JSONObject) device).contains("[]"));
        }
    }

    @Test
    @Order(2)
    void countDBDevices() throws Exception {
        var count = DeviceService.countDBDevices();
        assertNotNull(count);
    }

    @Test
    @Order(3)
    void testFindByMac() throws Exception {
        var device = DeviceService.findByMac("F7:AF:D0:EA:FC:1A");
        assertNotNull(device);

        device = DeviceService.findByMac("F7:AF:D0:EA:FC:XX");
        assertNull(device);
    }

    @Test
    @Order(4)
    void testFindLastConnected() throws Exception {
        var logs = DeviceService.findLastConnected("F7:AF:D0:EA:FC:1A");
        assertNotNull(logs);
        log.debug(logs.toString());
    }
}
