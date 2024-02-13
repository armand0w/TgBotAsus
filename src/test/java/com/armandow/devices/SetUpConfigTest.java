package com.armandow.devices;

import com.armandow.db.exceptions.DBException;
import com.armandow.devices.service.ASUSService;
import com.armandow.devices.utils.DevicesUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.sql.SQLException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SetUpConfigTest {
    public static MockedStatic<ASUSService> mockStatic = null;

    public static JSONObject loadJson(String name) {
        var json = new JSONObject();

        try {
            var is = DevicesBot.class.getClassLoader().getResourceAsStream("asusMock/" + name + ".json");
            if ( is != null ) {
                json = new JSONObject(new String(is.readAllBytes(), UTF_8));
                is.close();
            }
        } catch (Exception e) {
            log.error("Error al cargar archivo json");
        }

        assertNotNull(json);

        return json;
    }

    @Test
    @Order(1)
    void testLoadConfig() throws IOException {
        DevicesUtils.loadConfig();
        assertNotNull(DevicesUtils.getConfig());
        assertNotNull(DevicesUtils.getConfig().dataSource());
        assertNotNull(DevicesUtils.getConfig().dataSource().schema());
    }

    @Test
    @Order(2)
    void testBDConnect() throws Exception {
        DevicesUtils.dbConnect();
        var cnn = DevicesUtils.getDbConnection().executeQuery("SELECT 1", null);
        assertNotNull(cnn);
    }

    @Test
    @Order(3)
    void testTruncateTables() throws SQLException, DBException {
        DevicesUtils.getDbConnection().executeUpdate("PRAGMA foreign_keys = 0", null);


        DevicesUtils.getDbConnection().executeUpdate("DELETE FROM connection_logs", null);
        var data = DevicesUtils.getDbConnection().executeQuery("SELECT * FROM connection_logs", null);
        assertNotNull(data);
        assertNotNull(data.optQuery("/data"));
        assertEquals(0, data.optJSONArray("data").length());



        DevicesUtils.getDbConnection().executeUpdate("DELETE FROM device", null);
        data = DevicesUtils.getDbConnection().executeQuery("SELECT * FROM device", null);
        assertNotNull(data);
        assertNotNull(data.optQuery("/data"));
        assertEquals(0, data.optJSONArray("data").length());

        DevicesUtils.getDbConnection().executeUpdate("PRAGMA foreign_keys = 1", null);
    }

    @Test
    @Order(4)
    void mockStatic() {
        try {
            Mockito.clearAllCaches();
            mockStatic = Mockito.mockStatic(ASUSService.class);
            mockStatic.when(ASUSService::getAllClients).thenReturn(loadJson("clientList"));
            mockStatic.when(ASUSService::getCustomClientList).thenReturn(loadJson("customClientList").getString("custom_clientlist"));
            mockStatic.when(ASUSService::refreshToken).thenAnswer((Answer<Void>) invocation -> null);
            mockStatic.when(ASUSService::logout).thenAnswer((Answer<Void>) invocation -> null);

            assertNotNull(mockStatic);
        } catch (Exception e) {
            log.error("Error en initMockStatic", e);
        }
    }

}
