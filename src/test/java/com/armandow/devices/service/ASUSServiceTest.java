package com.armandow.devices.service;

import com.armandow.devices.utils.DevicesUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SuppressWarnings({"java:S5786"})
public class ASUSServiceTest {
    @BeforeAll
    static void beforeAll() throws Exception {
        DevicesUtils.loadConfig();
        ASUSService.refreshToken();
    }

    @AfterAll
    static void afterAll() throws Exception {
        ASUSService.logout();
    }

    @Test
    void testGetAllClients() throws Exception {
        var clients = ASUSService.getAllClients();
        assertNotNull(clients);
        log.debug(clients.toString());
    }

    @Test
    void getCustomClientList() throws Exception {
        var customClientList = ASUSService.getCustomClientList();
        assertNotNull(customClientList);

        var list = DevicesUtils.parseCustomClientList(customClientList);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }
}
