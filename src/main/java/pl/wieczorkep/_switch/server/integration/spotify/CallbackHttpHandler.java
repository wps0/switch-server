package pl.wieczorkep._switch.server.integration.spotify;

import com.sun.net.httpserver.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class CallbackHttpHandler implements HttpHandler {
    private final String hostname;
    // paskudnie to jest zrobione. Na pewno da się sporo optymalniej, po co hash mapa
    private static final HashMap<Integer, String> responseCodes = new HashMap<>(Map.ofEntries(
            new SimpleEntry<>(200, "<h1>200 OK</h1>"),
            new SimpleEntry<>(404, "<h1>404 Not Found</h1>No context found for request"),
            new SimpleEntry<>(405, "<h1>405 Method Not Allowed</h1>Request method not allowed")
    ));

    public CallbackHttpHandler(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        InetSocketAddress remoteHost = httpExchange.getRemoteAddress();
        final String logPrefix = "Connection from " + remoteHost.getAddress() + ":" + remoteHost.getPort() + ": ";

        // check request url
        URI requestUri = httpExchange.getRequestURI();
        // TODO: w query dawać temporary password
        if (!requestUri.getPath().equalsIgnoreCase("/callback")) {
            LOGGER.info(logPrefix + "404 Not Found");
            respondWithCode(httpExchange, 404);
            return;
        }

        // check request method
        String requestMethod = httpExchange.getRequestMethod();
        if (!requestMethod.equalsIgnoreCase("get")) {
            LOGGER.info(logPrefix + "405 Method Not Allowed");
            respondWithCode(httpExchange, 405);
            return;
        }

        // TODO: check tmp password
        // TODO: check query parameters

        LOGGER.info(logPrefix + "200 OK");
        LOGGER.debug(httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI() + " " + httpExchange.getRequestHeaders());
        respondWithCode(httpExchange, 200);
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
