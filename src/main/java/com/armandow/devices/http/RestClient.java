package com.armandow.devices.http;

import com.armandow.devices.utils.DevicesUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class RestClient {
    private static final HttpClient httpClient = createHttpClient();

    private final String url;
    @Getter @Setter private int statusCode;
    @Getter private Object body;
    @Setter private String produces;

    public RestClient(String url) {
        this.url = url;
    }

    public void postUrlEncode(Map<Object, Object> data) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .POST(ofFormData(data))
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("User-Agent", DevicesUtils.getConfig().router().agent())
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.trace(response.body());

        setStatusCode( response.statusCode() );
        setBody( response.body() );

        var headers = response.headers();
        headers.map().forEach((k, v) -> log.trace(k + ":" + v));
    }

    private HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for ( var entry : data.entrySet() ) {
            if ( !builder.isEmpty() ) {
                builder.append("&");
            }

            builder.append(URLEncoder.encode(entry.getKey().toString(), UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue().toString(), UTF_8));
            log.debug("POST: {}", builder);
        }

        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private void setBody(Object body) {
        if ( produces == null ) {
            this.body = body;
        } else {
            if ( this.produces.equals("application/json") ) {
                try {
                    this.body = new JSONObject(body.toString());
                } catch (Exception e) {
                    log.error("Parse JSON", e);
                    this.body = body;
                }
            }
        }
    }

    private static HttpClient createHttpClient() {
        HttpClient client;

        try {
            var context = SSLContext.getInstance("TLSv1.3");
            context.init(null, null, null);

            client = HttpClient.newBuilder()
                    .sslContext(context)
                    .cookieHandler(new CookieManager())
                    .connectTimeout(Duration.ofSeconds(25))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            client = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .connectTimeout(Duration.ofSeconds(25))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        }

        return client;
    }
}
