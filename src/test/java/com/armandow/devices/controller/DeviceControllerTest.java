package com.armandow.devices.controller;

import com.armandow.devices.service.ASUSService;
import com.armandow.devices.service.DeviceService;
import com.armandow.devices.utils.DevicesUtils;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({"java:S5786"})
public class DeviceControllerTest {

    @Test
    @Order(1)
    void testEmptyDB() throws Exception {
        assertEquals(0, DeviceService.countDBDevices());

        ASUSService.refreshToken();
        DevicesUtils.setDbDevices(new HashMap<>());
        var asusMap = DeviceController.parseAsusDevices();
        ASUSService.logout();

        assertNotNull(DevicesUtils.getDbDevices());
        assertNotNull(asusMap);

        log.debug("=== From ASUS Router");
        asusMap.forEach((k, v) -> {
            assertNotNull(k);
            assertNotNull(v);
            log.trace("{} {}", k, v.toString(2));

            try {
                DevicesUtils.getDbDevices().put(k, DeviceService.saveAsusDevice(v));
                assertNotNull(DevicesUtils.getDbDevices().get(k));
            } catch (Exception e) {
                log.error("SAVE NEW", e);
                Sentry.captureException(e);
            }
        });

        assertEquals(13, asusMap.size());
    }

    @Test
    @Order(2)
    void testLoadFromDB() {
        DevicesUtils.setDbDevices(DeviceController.loadDBDevices());
        assertNotNull(DevicesUtils.getDbDevices());

        log.debug("== From Data Base");
        DevicesUtils.getDbDevices().forEach((k, v) -> {
            assertNotNull(k);
            assertNotNull(v);
            log.trace("{} {}", k, v.toString(2));
        });

        assertEquals(13, DevicesUtils.getDbDevices().size());
    }
}
