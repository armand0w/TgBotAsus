package com.armandow.devices.task;

import com.armandow.devices.service.ASUSService;
import com.armandow.devices.service.DeviceService;
import com.armandow.devices.utils.DevicesUtils;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.armandow.devices.utils.Constants.*;

@Slf4j
public class VerifyDeviceMetadata implements Runnable {
    @Override
    public synchronized void run() {
        log.debug("== Updating devices metadata ==");
        List<JSONObject> customList = new ArrayList<>();

        try {
            ASUSService.refreshToken();
            customList = DevicesUtils.parseCustomClientList( ASUSService.getCustomClientList() );
            ASUSService.logout();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("parseCustomClientList", ie);
            Sentry.captureException(ie);
        } catch (Exception e) {
            log.error("parseCustomClientList", e);
            Sentry.captureException(e);
        }

        for ( var client : customList ) {
            var mac = client.getString(MAC_ADDR);
            if ( DevicesUtils.getDbDevices().containsKey(mac) ) {
                updateMetadata(mac, client);
            } else {
                insertMetadata(mac, client);
            }
        }
    }

    private void updateMetadata(String mac, JSONObject device) {
        try {
            log.trace("updateMetadata: {}", mac);
            var modify = false;
            var current = DevicesUtils.getDbDevices().get(mac);

            var newGroup = device.optString(GROUP);
            var newNickName = device.optString(NICKNAME);

            var currentGroup = current.optString(V_GROUP);
            var currentNickName = current.optString(V_NICKNAME);

            if ( newGroup != null
                    && !newGroup.isEmpty()
                    && !newGroup.equals(currentGroup) ) {
                current.put(V_GROUP, newGroup);
                modify = true;
            }

            if ( newNickName != null
                    && !newNickName.isEmpty()
                    && !newNickName.equals(currentNickName) ) {
                current.put(V_NICKNAME, newNickName);
                modify = true;
            }

            if ( modify ) {
                log.trace("CURRENT [{}] [{}]", currentGroup, currentNickName);
                log.trace("NEW [{}] [{}]", newGroup, newNickName);
                log.info("Modificacion a dispositivo [{}] [{}]", mac, DevicesUtils.getNotEmptyName(current));
                DeviceService.updateMetaData(device);
                DevicesUtils.getDbDevices().replace(mac, current);
            }
        } catch (Exception e) {
            log.error("updateMetadata", e);
            Sentry.captureException(e);
        }

    }

    private void insertMetadata(String mac, JSONObject newDevice) {
        try {
            log.trace("insertMetadata: {}", newDevice);
            var auxDevice = new JSONObject()
                    .put(C_IS_ONLINE, newDevice.get(IS_ONLINE))
                    .put(V_MAC_ADDR, newDevice.get(MAC_ADDR))
                    .put(V_NICKNAME, newDevice.opt(NICKNAME))
                    .put(V_GROUP, newDevice.opt(GROUP));

            DevicesUtils.getDbDevices().put(mac, auxDevice);
            DeviceService.insertMetaData(newDevice);
        } catch (Exception e) {
            log.error("insertMetadata", e);
            Sentry.captureException(e);
        }
    }
}
