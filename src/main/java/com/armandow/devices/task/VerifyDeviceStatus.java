package com.armandow.devices.task;

import com.armandow.devices.service.ASUSService;
import com.armandow.devices.service.DeviceService;
import com.armandow.devices.utils.DevicesUtils;
import com.armandow.telegrambotapi.methods.SendMessage;
import com.armandow.telegrambotapi.utils.Emoji;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Time;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import static com.armandow.devices.utils.Constants.*;

@Slf4j
public class VerifyDeviceStatus implements Runnable {
    private JSONObject allClients;

    @Override
    public synchronized void run() {
        log.debug("== Updating status devices ==");
        updateStatus(true);
    }

    public void updateStatus(boolean notify) {
        var msgBuilder = new StringBuilder();

        try {
            ASUSService.refreshToken();
            allClients = ASUSService.getAllClients();
            var connectedDevices = ((JSONArray) allClients.optQuery("/get_clientlist/maclist")).toList();

            DevicesUtils.getDbDevices().forEach((m, dbDevice) -> {
                var updateDevice = false;
                var auxIsOnline = dbDevice.getString(C_IS_ONLINE);

                if ( connectedDevices.contains(m) ) {
                    if ( auxIsOnline.equals("N") ) {
                        updateDevice = true;
                        dbDevice.put(C_IS_ONLINE, "S");
                        updateMetaData(m, dbDevice);
                        msgBuilder.append(buildDeviceMsg(m, dbDevice));
                    }

                    connectedDevices.remove(m);
                } else if ( auxIsOnline.equals("S") ) {
                    updateDevice = true;
                    dbDevice.put(C_IS_ONLINE, "N");
                    msgBuilder.append(buildDeviceMsg(m, dbDevice));
                }

                if ( updateDevice ) {
                    try {
                        DeviceService.updateDevice(dbDevice);
                    } catch (Exception e) {
                        log.error("UPDATE device", e);
                        Sentry.captureException(e);
                    }
                }
            });

            if ( notify && !msgBuilder.isEmpty() ) {
                var message = new SendMessage();
                message.setChatId(DevicesUtils.getConfig().bot().userId());
                message.setText(msgBuilder.toString());
                message.send();
            }

            searchNewDevices(connectedDevices);
            ASUSService.logout();
        } catch (Exception e) {
            log.error("RUN", e);
            Sentry.captureException(e);
        }
    }

    private String buildDeviceMsg(String mac, JSONObject dbDevice) {
        var msg = "";

        try {
            var last = DeviceService.findLastConnected(mac);

            var color = Emoji.GREEN_COLOR;
            if ( dbDevice.getString(C_IS_ONLINE).equals("N") ) {
                color = Emoji.RED_COLOR;
            }

            if ( last != null ) {
                var dateLast = DevicesUtils.parseSQLDateTime(last.getString("d_date"));
                var minutes = Duration.between(new Time(dateLast.getTime()).toLocalTime(),
                        new Time(new Date().getTime()).toLocalTime());

                log.debug(">>>>>>>>>> Seconds elapsed: [{}] :: {} <<<<<<<<<<", minutes.toSeconds(), DevicesUtils.getConfig().scheduler().notifyInterval());
                if ( minutes.toSeconds() < 0 || minutes.toSeconds() > DevicesUtils.getConfig().scheduler().notifyInterval() ) {
                    msg = color + " " + DevicesUtils.getNotEmptyName(dbDevice) + "\n\n";
                }
            } else msg = color + " " + DevicesUtils.getNotEmptyName(dbDevice) + "\n\n";
        } catch (Exception e) {
            log.error("buildDeviceMsg", e);
            Sentry.captureException(e);
        }

        return msg;
    }

    void searchNewDevices(List<Object> connected) {
        if ( !connected.isEmpty() ) {
            for ( var mac: connected ) {
                try {
                    var asusDevice = (JSONObject) allClients.optQuery("/get_clientlist/" + mac);
                    DevicesUtils.getDbDevices().put(mac.toString(), DeviceService.saveAsusDevice(asusDevice));

                    log.info("=----> Nuevo dispositivo registrado [{}] [{}]",
                            asusDevice.get(MAC_ADDR),
                            asusDevice.get(NAME));

                    var message = new SendMessage();
                    message.setChatId(DevicesUtils.getConfig().bot().userId());
                    message.setText(
                            Emoji.NO_MOBILE_PHONES + " Nuevo dispositivo conectado\n" +
                                    "Name: " + asusDevice.get(NAME) + "\n" +
                                    "IP: " + asusDevice.get(IP) + "\n" +
                                    "Vendor: " + asusDevice.get(VENDOR));
                    message.send();
                } catch (Exception e) {
                    log.error("searchNewDevices {}: ", mac, e);
                }
            }
        }
    }

    void updateMetaData(String mac, JSONObject dbDevice) {
        var asusDevice = (JSONObject) allClients.query("/get_clientlist/" + mac);

        var dbName = dbDevice.optString(V_NAME);
        var dbNickName = dbDevice.optString(V_NICKNAME);
        var dbVendor = dbDevice.optString(V_VENDOR);

        var asusName = asusDevice.optString(NAME);
        var asusNickName = asusDevice.optString(NICKNAME);
        var asusVendor = asusDevice.optString(VENDOR);

        if ( asusName != null
                && !asusName.isEmpty()
                && !asusName.equals(dbName) ) {
            dbDevice.put(V_NAME, asusName.trim());
        }

        if ( asusNickName != null
                && !asusNickName.isEmpty()
                && !asusNickName.equals(dbNickName) ) {
            dbDevice.put(V_NICKNAME, asusNickName.trim());
        }

        if ( asusVendor != null
                && !asusVendor.isEmpty()
                && !asusVendor.equals(dbVendor) ) {
            dbDevice.put(V_VENDOR, asusVendor.trim());
        }

        log.trace(dbDevice.toString(2));
    }
}
