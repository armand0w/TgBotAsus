package com.armandow.devices.service;

import com.armandow.devices.http.RestClient;
import com.armandow.devices.records.Router;
import com.armandow.devices.utils.DevicesUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

@Slf4j
public class ASUSService {
    private static final Router routerProps = DevicesUtils.getConfig().router();
    private static final String APPLICATION_JSON = "application/json";

    private ASUSService() {
        // Prevent instance
    }

    /**
     * Metodo para login con el router
     * @throws Exception base64 y postUrlEncode
     */
    public static void refreshToken() throws Exception {
        var data = new HashMap<>();
        data.put("login_authorization",
                Base64.getEncoder().encodeToString( String.format("%s:%s", routerProps.user(), routerProps.password()).getBytes() ));

        var restClient = new RestClient(routerProps.ip() + "/login.cgi");
        restClient.setProduces(APPLICATION_JSON);
        restClient.postUrlEncode(data);

        log.debug("login StatusCode: {}", restClient.getStatusCode() );
        log.trace("login Body: {}", restClient.getBody() );

        if ( restClient.getStatusCode() != 200 ) {
            throw new IOException(restClient.getBody().toString());
        }
    }

    /**
     * Obtiene lista de los clientes recientes (Conectados y no conectados)
     * @return JSONObject
     * @throws Exception postUrlEncode
     */
    public static JSONObject getAllClients() throws Exception {
        var data = new HashMap<>();
        data.put("hook", "get_clientlist(appobj);wl_sta_list_2g(appobj);wl_sta_list_5g(appobj);wl_sta_list_5g_2(appobj)");

        var restClient = new RestClient(routerProps.ip() + "/appGet.cgi");
        restClient.setProduces(APPLICATION_JSON);
        restClient.postUrlEncode(data);

        log.debug("allClients StatusCode: {}", restClient.getStatusCode() );
        log.trace("allClients Body: {}", restClient.getBody() );

        return (JSONObject) restClient.getBody();
    }

    /**
     * Obtiene las modificaciones de nombres de los dispositivos, como nombre, grupo etc
     * @return String para parsearse
     * @throws IOException Cuando no se obtuvo resultado
     * @throws InterruptedException postUrlEncode
     * @throws InterruptedException Cuando se obtiene httpStatus != 200
     */
    public static String getCustomClientList() throws IOException, InterruptedException {
        var data = new HashMap<>();
        data.put("hook", "nvram_get(custom_clientlist)");

        var restClient = new RestClient(routerProps.ip() + "/appGet.cgi");
        restClient.setProduces(APPLICATION_JSON);
        restClient.postUrlEncode(data);

        log.debug("customClientList StatusCode: {}", restClient.getStatusCode() );
        log.trace("customClientList Body: {}", restClient.getBody() );

        if ( restClient.getStatusCode() != 200 ) {
            throw new IOException(restClient.getBody().toString());
        }

        var customList = ((JSONObject) restClient.getBody()).get("custom_clientlist");

        if ( customList == null ) {
            throw new IOException("Invalid custom_clientlist");
        }

        log.trace("customClientList string: {}", customList);

        return customList.toString();
    }

    /**
     * Termina la sesion con el router
     * @throws Exception postUrlEncode
     */
    public static void logout() throws Exception {
        var restClient = new RestClient(routerProps.ip() + "/Logout.asp");
        restClient.postUrlEncode(new HashMap<>());

        log.debug("logout StatusCode: {}", restClient.getStatusCode() );
        log.trace("logout Body: {}", restClient.getBody() );
    }
}
