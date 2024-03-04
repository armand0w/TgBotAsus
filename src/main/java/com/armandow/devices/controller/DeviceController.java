package com.armandow.devices.controller;

import com.armandow.devices.service.ASUSService;
import com.armandow.devices.service.DeviceService;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.armandow.devices.utils.Constants.V_MAC_ADDR;

@Slf4j
public class DeviceController {
    private DeviceController() {
        // Prevent instance
    }

    /**
     * Obtiene los dispositivos que estan en la Base de datos
     * @return {@code HashMap<String, JSONObject>}
     */
    public static Map<String, JSONObject> loadDBDevices() {
        var mapDevices = new HashMap<String, JSONObject>();

        try {
            var dbDevices = DeviceService.getAllDevices();
            var data = dbDevices.getJSONArray("data");
            for ( var i = 0; i < data.length(); i++ ) {
                var device = data.getJSONObject(i);
                mapDevices.put(device.getString(V_MAC_ADDR), device);
            }
        } catch (Exception e) {
            log.error("loadDBDevices", e);
            Sentry.captureException(e);
        }

        return mapDevices;
    }

    /**
     * Obtiene los dispositivos con informacion disponible,
     * conectados y no conectados
     * @return {@code HashMap<String, JSONObject>}
     * @throws Exception from ASUSService.getAllClients()
     */
    public static Map<String, JSONObject> parseAsusDevices() throws Exception {
        var asusMap = new HashMap<String, JSONObject>();
        var clientList = ASUSService.getAllClients().getJSONObject("get_clientlist");
        var elements = clientList.keys();

        while ( elements.hasNext() ) {
            var mac = elements.next();
            if ( !mac.equals("maclist") && !mac.equals("ClientAPILevel") ) {
                asusMap.put(mac, clientList.getJSONObject(mac));
            }
        }

        return asusMap;
    }
}
