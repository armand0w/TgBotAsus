package com.armandow.devices.utils;

import com.armandow.db.DBSQLite;
import com.armandow.devices.DevicesBot;
import com.armandow.devices.records.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.armandow.devices.utils.Constants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class DevicesUtils {
    private DevicesUtils() {
        // Util
    }

    private static Config config;
    private static DBSQLite dbConnection;
    private static Map<String, JSONObject> dbDevices = null;

    public static synchronized Config getConfig() {
        return config;
    }

    public static synchronized DBSQLite getDbConnection() {
        return dbConnection;
    }

    public static synchronized Map<String, JSONObject> getDbDevices() {
        return dbDevices;
    }

    public static synchronized void setDbDevices(Map<String, JSONObject> dbDevices) {
        DevicesUtils.dbDevices = dbDevices;
    }

    public static String getRelease() {
        return DevicesBot.class.getPackage().getImplementationVersion();
    }

    public static void loadConfig() throws IOException {
        var jsonConfig = loadConfigFile();
        log.trace(jsonConfig.toString(2));

        var sentry = new SentryCnf(
                jsonConfig.query("/sentry/dsn").toString(),
                jsonConfig.query("/sentry/environment").toString());
        var bot = new Bot(
                System.getenv("BOT_TOKEN"),
                jsonConfig.query("/bot/username").toString(),
                Long.valueOf(System.getenv("USER_ID")),
                Long.valueOf(jsonConfig.query("/bot/apiUpdate").toString()));
        var router = new Router(
                jsonConfig.query("/router/agent").toString(),
                jsonConfig.query("/router/ip").toString(),
                jsonConfig.query("/router/user").toString(),
                jsonConfig.query("/router/password").toString());
        var dataSource = new DataSource(jsonConfig.query("/datasource/schema").toString());
        var scheduler = new Scheduler(
                (Integer) jsonConfig.query("/scheduler/updateDevicesStatus"),
                (Integer) jsonConfig.query("/scheduler/updateDevicesMetadata"),
                (Integer) jsonConfig.query("/scheduler/notifyInterval"));

        config = new Config(sentry, bot, router, dataSource, scheduler);
    }

    public static void dbConnect() throws IOException {
        if ( config == null ) {
            throw new IOException("config.json Not Loaded");
        }

        var dataSource = config.dataSource();
        if ( dataSource == null ) {
            throw new IOException("dataSource Not Loaded");
        }

        if ( dataSource.schema() == null ){
            throw new IllegalArgumentException("datasource values incomplete");
        }

        dbConnection = new DBSQLite(dataSource.schema());

        try {
            dbConnection.executeCountQuery("SELECT COUNT(1) FROM device");
        } catch (Exception e) {
            Schema.init();
        }
    }

    public static Date parseSQLDateTime(String dateTime) throws ParseException {
        var formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.parse(dateTime);
    }

    public static String parseToSQLFormat(Date dateTime) {
        var formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(dateTime);
    }

    public static String getNotEmptyName(JSONObject dbDevice) {
        var auxName = new StringBuilder(); // Unknown

        var nickName = dbDevice.optString(V_NICKNAME);
        var name = dbDevice.optString(V_NAME);
        var vendor = dbDevice.optString(V_VENDOR);

        if ( nickName != null ) {
            auxName.append(nickName.trim());
        }

        if ( name != null ) {
            if ( auxName.isEmpty() )
                auxName.append(name.trim());
            else if ( !auxName.toString().contains("[") && hasLength(name) )
                auxName.append(" [").append(name.trim()).append("]");
        }

        if ( vendor != null ) {
            if ( auxName.isEmpty() )
                auxName.append(vendor.trim());
            else if ( !auxName.toString().contains("[") && hasLength(vendor) )
                auxName.append(" [").append(vendor.trim()).append("]");
        }

        if ( auxName.isEmpty() || auxName.toString().isEmpty() )
            auxName.append("Unknown");

        return auxName.toString();
    }

    public static List<JSONObject> parseCustomClientList(String customClientList)  {
        var parsedClientList = new ArrayList<JSONObject>();
        if ( customClientList != null ) {
            for ( var customClient : customClientList
                    .replace("&#62", ">")
                    .replace("&#60", "<")
                    .split("<") ) {
                var device = customClient.split(">");
                if ( device.length > 2 ) {
                    var json = new JSONObject()
                            .put(IS_ONLINE, "N")
                            .put(MAC_ADDR, device[1].trim());

                    var nickName = URLDecoder.decode(device[0].trim(), UTF_8).replace("&#195&#177", "ñ");
                    if ( !nickName.isEmpty() )
                        json.put(NICKNAME, nickName);

                    if ( device.length > 5 )
                        json.put(GROUP, URLDecoder.decode(device[6].trim(), UTF_8));

                    parsedClientList.add(json);
                } //end if
            } // end for
        }

        return parsedClientList;
    }

    private static JSONObject loadConfigFile() throws IOException {
        var jsonConfig = new JSONObject();
        var configFile = new File("config.json");

        if ( configFile.exists() && configFile.canRead() && configFile.canWrite() ) {
            log.debug("Load external config.json: {}", configFile.getAbsolutePath());
            var fileBytes = Files.readAllBytes(configFile.toPath());
            jsonConfig = new JSONObject(new String(fileBytes, UTF_8));
        } else {
            var is = DevicesBot.class.getClassLoader().getResourceAsStream(configFile.getName());
            if ( is != null ) {
                log.trace("Load in jar config.json");
                jsonConfig = new JSONObject(new String(is.readAllBytes(), UTF_8));
                is.close();
            }
        }

        return jsonConfig;
    }

    public static boolean hasLength(String string) {
        return string != null && !string.isEmpty();
    }

    public static boolean isAuthorized(JSONObject user) {
        try {
            var uId = user.getLong("id");
            return ( uId == config.bot().userId() );
        } catch (Exception r) {
            // ignore
        }

        return false;
    }
}
