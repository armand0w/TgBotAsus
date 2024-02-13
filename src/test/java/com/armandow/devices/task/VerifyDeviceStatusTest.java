package com.armandow.devices.task;

import com.armandow.devices.SetUpConfigTest;
import com.armandow.devices.service.ASUSService;
import com.armandow.devices.utils.DevicesUtils;
import com.armandow.telegrambotapi.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({"java:S5786"})
public class VerifyDeviceStatusTest {
    @Test
    @Order(1)
    void testInitBot() {
        var bot = new TelegramBot(DevicesUtils.getConfig().bot().token());

        assertNotNull(bot);
        assertNotNull(DevicesUtils.getDbDevices());
    }

    @Test
    @Order(2)
    void testRun() throws InterruptedException {
        SetUpConfigTest.mockStatic.when(ASUSService::getAllClients).thenReturn(SetUpConfigTest.loadJson("clientList2"));
        await().atMost(3, SECONDS);
        var status = new VerifyDeviceStatus();
        status.run();

        assertNotNull(status);
    }
}
