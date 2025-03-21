package com.armandow.devices.service;

import com.armandow.devices.utils.DevicesUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertTrue(clients.has("get_clientlist"));
        assertTrue(clients.has("wl_sta_list_2g"));
        assertTrue(clients.has("wl_sta_list_5g"));
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
