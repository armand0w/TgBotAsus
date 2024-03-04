package com.armandow.devices.command;

import com.armandow.devices.utils.DevicesUtils;
import com.armandow.telegrambotapi.TelegramBot;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({"java:S5786"})
public class CurrentCommandTest {

    @Test
    @Order(1)
    void testInitBot() {
        var bot = new TelegramBot(DevicesUtils.getConfig().bot().token());

        assertNotNull(bot);
        assertNotNull(DevicesUtils.getDbDevices());
    }

    @Test
    @Order(2)
    void execute() {
        var command = new CurrentCommand();
        var json = new JSONObject().put("id", DevicesUtils.getConfig().bot().userId());
        command.execute(json, json, null);

        assertNotNull(command);
    }
}
