package pl.wieczorkep._switch.server.integration.spotify;

import com.sun.net.httpserver.*;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.core.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class CallbackHttpHandler implements HttpHandler {
    private String password;
    private final SpotifyApiGateway spotifyApi;
    // paskudnie to jest zrobione. Na pewno da się sporo optymalniej, po co hash mapa
    private static final HashMap<Integer, String> responseCodes = new HashMap<>(Map.ofEntries(
            new SimpleEntry<>(200, "<h1>200 OK</h1>"),
            new SimpleEntry<>(400, "<h1>400 Bad Request</h1>"),
            new SimpleEntry<>(401, "<h1>401 Unauthorized</h1>"),
            new SimpleEntry<>(404, "<h1>404 Not Found</h1>No context found for request"),
            new SimpleEntry<>(405, "<h1>405 Method Not Allowed</h1>Request method not allowed"),
            new SimpleEntry<>(500, "<h1>500 Internal Server Error</h1>")
    ));

    public CallbackHttpHandler(SpotifyApiGateway spotifyApi) {
        this.spotifyApi = spotifyApi;
        this.password = Utils.generatePassword(32);
        LOGGER.info(String.format("Make sure to manually insert one-time password string to your request (otherwise you" +
                " will get 401 error): &password=%s", this.password));
    }

    @Override
    @Synchronized
    public void handle(HttpExchange httpExchange) throws IOException {
        InetSocketAddress remoteHost = httpExchange.getRemoteAddress();
        final String logPrefix = "Connection from " + remoteHost.getAddress() + ":" + remoteHost.getPort() + ": ";

        URI requestUri = httpExchange.getRequestURI();
        // check request url
        if (!requestUri.getPath().equalsIgnoreCase("/callback")) {
            LOGGER.info(logPrefix + "404 Not Found");
            respondWithCode(httpExchange, 404);
            return;
        }

        String rawQuery = requestUri.getRawQuery();
        // verify length
        if (rawQuery == null || rawQuery.isBlank()) {
            LOGGER.info(logPrefix + "400 Bad Request (query is empty)");
            respondWithCode(httpExchange, 400);
            return;
        }
        if (rawQuery.length() > 600) {
            LOGGER.info(logPrefix + "400 Bad Request (query too long - " + rawQuery.length() + " chars)");
            respondWithCode(httpExchange, 400);
            return;
        }

        // check request method
        String requestMethod = httpExchange.getRequestMethod();
        if (!requestMethod.equalsIgnoreCase("get")) {
            LOGGER.info(logPrefix + "405 Method Not Allowed");
            respondWithCode(httpExchange, 405);
            return;
        }

        if (password == null || password.isBlank()) {
            LOGGER.warn(logPrefix + "500 Internal Server Error (temporary password is empty)");
            respondWithCode(httpExchange, 500);
            return;
        }

        // check tmp password
        if (!getQueryArgument("password", rawQuery).equals(password)) {
            LOGGER.info(logPrefix + "401 Unauthorized (passwords do not match)");
            respondWithCode(httpExchange, 401);
            return;
        }

        // check query parameters
        String code = getQueryArgument("code", rawQuery);
        if (code.isBlank()) {
            LOGGER.info(logPrefix + "400 Bad Request (code query param not specified)");
            respondWithCode(httpExchange, 400);
            return;
        }

        this.password = "";
        // exchange code for spotify auth data
        (new Thread(() -> spotifyApi.exchangeAccessCodeToAuthToken(code))).start();
        (new Thread(spotifyApi::stopServer)).start();
        LOGGER.info(logPrefix + "200 OK");
        respondWithCode(httpExchange, 200);
    }

    @NonNull
    private String getQueryArgument(@NonNull String name, String queryParams) {
        int i = 0;
        int j = 0;
        while (j < name.length() && i < queryParams.length()) {
            if (name.charAt(j) == queryParams.charAt(i)) {
                j++;
            } else {
                j = 0;
            }
            i++;
        }
        i++;

        StringBuilder value = new StringBuilder();
        while (i < queryParams.length()) {
            if (queryParams.charAt(i) == '&') {
                break;
            } else {
                value.append(queryParams.charAt(i));
            }
            i++;
        }
        return value.toString();
    }

    private void respondWithCode(HttpExchange httpExchange, int code)  throws IOException {
        try (InputStream body = httpExchange.getRequestBody()) {
            body.readAllBytes();
        }
        setHeaders(httpExchange);
        String responseStr = responseCodes.get(code);
        httpExchange.sendResponseHeaders(code, responseStr.length());
        httpExchange.getResponseBody().write(responseStr.getBytes());
        httpExchange.getResponseBody().close();
        httpExchange.close();
    }

    private void setHeaders(HttpExchange httpExchange) {
        Headers responseHeaders = httpExchange.getResponseHeaders();
        // The response may be stored by any cache, even if the response is normally non-cacheable. However, the stored
        //  response MUST always go through validation with the origin server first before using it.
        responseHeaders.add("Cache-Control", "no-cache");
        responseHeaders.add("Content-Type", "text/html");
    }
}
