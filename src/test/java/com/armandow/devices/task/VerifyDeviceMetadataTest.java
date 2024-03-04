package com.armandow.devices.task;

import com.armandow.devices.utils.DevicesUtils;
import com.armandow.telegrambotapi.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({"java:S5786"})
public class VerifyDeviceMetadataTest {
    @Test
    @Order(1)
    void testInitBot() throws Exception {
        var bot = new TelegramBot(DevicesUtils.getConfig().bot().token());

        assertNotNull(bot);
        assertNotNull(DevicesUtils.getDbDevices());
    }

    @Test
    @Order(2)
    void testRun() {
        var meta = new VerifyDeviceMetadata();
        meta.run();

        assertNotNull(meta);
    }
}
