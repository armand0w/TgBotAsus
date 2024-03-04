package com.armandow.devices.service;

import com.armandow.db.exceptions.DBException;
import com.armandow.devices.utils.DevicesUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;

import static com.armandow.devices.utils.Constants.*;

@Slf4j
public class DeviceService {
    private DeviceService() {
        // DeviceService
    }

    public static Integer countDBDevices() throws SQLException, DBException {
        return DevicesUtils.getDbConnection().executeCountQuery("SELECT COUNT(1) AS count FROM device");
    }

    public static JSONObject getAllDevices() throws SQLException, DBException {
        var result = DevicesUtils.getDbConnection().executeQuery("SELECT * FROM device", null);
        result.remove("columns");
        return result;
    }

    /**
     * Guarda un dispositivo en la base de datos
     * @param device (Con formato de ASUS)
     * @return device (Con formato de BD)
     * @throws Exception from JSON & MARIA
     */
    public static JSONObject saveAsusDevice(JSONObject device) throws Exception {
        log.trace(device.toString(2));

        var isOnline = "N";
        var interfaceType = "";

        var isOnlineValue = device.optString(IS_ONLINE);
        if ( isOnlineValue!=null && isOnlineValue.equals("1") ) {
            isOnline = "S";
        }

        interfaceType = switch ( device.optString("isWL") ) {
            case "0" -> "eth0";
            case "1" -> "2Ghz";
            case "2" -> "5Ghz";
            default -> "wired";
        };
        
        var count = DevicesUtils.getDbConnection()
                .executeQuery("SELECT COUNT(1) AS count FROM device WHERE v_mac_addr = ?",
                        new JSONArray().put(new JSONObject().put("type", "string").put("value", device.get(MAC_ADDR))));
        
        if ( Integer.parseInt(count.optQuery("/data/0/count").toString()) > 0 ) {
            var updateBuilder = new StringBuilder("UPDATE device SET ");
            if ( DevicesUtils.hasLength(device.getString(NAME)) ) {
                updateBuilder.append("v_name = \"").append(device.getString(NAME)).append("\", ");
            }
            if ( DevicesUtils.hasLength(device.getString(NICKNAME)) ) {
                updateBuilder.append("v_nickname = \"").append(device.getString(NICKNAME)).append("\", ");
            }
            if ( DevicesUtils.hasLength(device.getString(VENDOR)) ) {
                updateBuilder.append("v_vendor = \"").append(device.getString(VENDOR)).append("\", ");
            }
            if ( DevicesUtils.hasLength(device.getString(IP)) ) {
                updateBuilder.append("v_ip = \"").append(device.getString(IP)).append("\", ");
            }
            if ( DevicesUtils.hasLength(device.getString(INTERFACE)) ) {
                updateBuilder.append("v_interface = \"").append(device.getString(INTERFACE)).append("\", ");
            }
            if ( DevicesUtils.hasLength(device.getString(GROUP)) ) {
                updateBuilder.append("v_group = \"").append(device.getString(GROUP)).append("\", ");
            }
            if ( DevicesUtils.hasLength(device.getString(IS_ONLINE)) ) {
                updateBuilder.append("c_is_online = \"").append(device.getString(IS_ONLINE)).append("\", ");
            }
            
            if ( updateBuilder.length() > 1 ) {
                updateBuilder.append("d_update_date = DATETIME(CURRENT_TIMESTAMP, 'localtime') WHERE v_mac_addr = \"").append(device.get(MAC_ADDR)).append("\"");
                log.debug("UPDATE QUERY :: {}", updateBuilder);
                DevicesUtils.getDbConnection().executeUpdate(updateBuilder.toString(), null);
            }
        } else {
            DevicesUtils.getDbConnection()
                    .executeUpdate("INSERT INTO device(i_id, v_name, v_nickname, v_mac_addr, v_vendor, v_ip, v_interface, v_group, c_is_online, d_register_date) " +
                                    "VALUES ((SELECT (COUNT(1) + 1) FROM device), ?, ?, ?, ?, ?, ?, ?, ?, DATETIME(CURRENT_TIMESTAMP, 'localtime'))",
                            new JSONArray()
                                    .put(new JSONObject().put("type", "string").put("value", device.optString(NAME)))
                                    .put(new JSONObject().put("type", "string").put("value", device.optString(NICKNAME)))
                                    .put(new JSONObject().put("type", "string").put("value", device.get(MAC_ADDR)))
                                    .put(new JSONObject().put("type", "string").put("value", device.get(VENDOR)))
                                    .put(new JSONObject().put("type", "string").put("value", device.get(IP)))
                                    .put(new JSONObject().put("type", "string").put("value", interfaceType))
                                    .put(new JSONObject().put("type", "string").put("value", device.get(GROUP)))
                                    .put(new JSONObject().put("type", "string").put("value", isOnline)));
        }

        return new JSONObject()
                .put(V_NAME, device.optString(NAME))
                .put(V_NICKNAME, device.optString(NICKNAME))
                .put(V_MAC_ADDR, device.get(MAC_ADDR))
                .put(V_VENDOR, device.optString(VENDOR))
                .put(V_IP, device.get(IP))
                .put(V_INTERFACE, interfaceType)
                .put(V_GROUP, device.optString(GROUP))
                .put(C_IS_ONLINE, isOnline);
    }

    /**
     * Guarda info de dispositivo
     * @param device en formado DB
     * @throws Exception JSONObject & executeSP
     */
    public static void updateDevice(JSONObject device) throws Exception {
        log.trace(device.toString(2));

        var updateBuilder = new StringBuilder("UPDATE device SET ");
        if ( DevicesUtils.hasLength(device.optString(V_NAME)) ) {
            updateBuilder.append("v_name = \"").append(device.getString(V_NAME)).append("\", ");
        }
        if ( DevicesUtils.hasLength(device.optString(V_NICKNAME)) ) {
            updateBuilder.append("v_nickname = \"").append(device.getString(V_NICKNAME)).append("\", ");
        }
        if ( DevicesUtils.hasLength(device.optString(V_VENDOR)) ) {
            updateBuilder.append("v_vendor = \"").append(device.getString(V_VENDOR)).append("\", ");
        }
        if ( DevicesUtils.hasLength(device.optString(V_IP)) ) {
            updateBuilder.append("v_ip = \"").append(device.getString(V_IP)).append("\", ");
        }
        if ( DevicesUtils.hasLength(device.optString(V_INTERFACE)) ) {
            updateBuilder.append("v_interface = \"").append(device.getString(V_INTERFACE)).append("\", ");
        }
        if ( DevicesUtils.hasLength(device.optString(V_GROUP)) ) {
            updateBuilder.append("v_group = \"").append(device.getString(V_GROUP)).append("\", ");
        }
        if ( DevicesUtils.hasLength(device.optString(C_IS_ONLINE)) ) {
            updateBuilder.append("c_is_online = \"").append(device.getString(C_IS_ONLINE)).append("\", ");
        }

        if ( updateBuilder.length() > 1 ) {
            updateBuilder.append("d_update_date = DATETIME(CURRENT_TIMESTAMP, 'localtime') WHERE v_mac_addr = \"").append(device.get(V_MAC_ADDR)).append("\"");
            log.debug(">>>>>>>   UPDATE QUERY :: {}", updateBuilder);
            DevicesUtils.getDbConnection().executeUpdate(updateBuilder.toString(), null);
        }
    }

    public static JSONObject findByMac(String mac) throws Exception {
        log.trace("findByMac: " + mac);
        var query = "SELECT * FROM device WHERE v_mac_addr = ?";
        var result = DevicesUtils.getDbConnection().executeQuery(query,
                new JSONArray().put(new JSONObject().put("type", STRING).put(VALUE, mac)));
        var data = result.optJSONArray("data");

        if ( !data.isEmpty() ) {
            result.remove("columns");
            log.debug(result.toString(2));
            return (JSONObject) result.query("/data/0");
        }

        return null;
    }

    public static JSONObject findLastConnected(String mac) throws Exception {
        log.trace("findLastConnected: " + mac);
        var query = "SELECT CL.* FROM device D INNER JOIN connection_logs CL on D.i_id = CL.i_device_id_fk WHERE D.v_mac_addr = ? ORDER BY CL.i_id DESC LIMIT 1";
        var result = DevicesUtils.getDbConnection().executeQuery(query, new JSONArray().put(new JSONObject().put("type", STRING).put(VALUE, mac)));
        var data = result.optQuery("/data/0");

        if ( data != null ) {
            log.debug(data.toString());
            return (JSONObject) data;
        }

        return null;
    }

    public static void insertMetaData(JSONObject metadata) throws Exception {
        log.trace(metadata.toString(2));
        var insert = "INSERT INTO device ( v_mac_addr, c_is_online, v_nickname, v_group, d_register_date ) VALUES (?, ?, ?, ?, DATETIME(CURRENT_TIMESTAMP, 'localtime'))";
        var parameters = new JSONArray()
                .put(new JSONObject().put("type", STRING).put(VALUE,  metadata.get(MAC_ADDR)))
                .put(new JSONObject().put("type", STRING).put(VALUE,  metadata.get(IS_ONLINE)))
                .put(new JSONObject().put("type", STRING).put(VALUE, metadata.opt(NICKNAME)))
                .put(new JSONObject().put("type", STRING).put(VALUE, metadata.opt(GROUP)));

        log.trace(parameters.toString(2));
        DevicesUtils.getDbConnection().executeUpdate(insert, parameters);
    }

    public static void updateMetaData(JSONObject metadata) throws Exception {
        log.trace(metadata.toString(2));
        var update = "UPDATE device SET v_nickname = ?, v_group = ? WHERE v_mac_addr = ?";
        var parameters = new JSONArray()
                .put(new JSONObject().put("type", STRING).put(VALUE, metadata.opt(NICKNAME)))
                .put(new JSONObject().put("type", STRING).put(VALUE, metadata.opt(GROUP)))
                .put(new JSONObject().put("type", STRING).put(VALUE,  metadata.opt(MAC_ADDR)));

        log.trace(parameters.toString(2));
        DevicesUtils.getDbConnection().executeUpdate(update, parameters);
    }
}
