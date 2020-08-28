package pl.wieczorkep._switch.server.integration.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.net.httpserver.HttpServer;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import pl.wieczorkep._switch.server.SoundServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.Base64;

import static java.lang.String.format;
import static pl.wieczorkep._switch.server.Constants.*;
import static pl.wieczorkep._switch.server.core.utils.Utils.stripQuery;
import static pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway.AuthMethod.BASIC;

@Log4j2
public class SpotifyApiGateway {
    private final String appClientId;
    private final String appClientSecret;
    private final String appScopes;
    private final String appCallbackUrl;
    private String authToken;
    private String refreshToken;
    @Getter @Setter
    private int validity;
    @Getter @Setter
    private long lastRefresh;
    private final SoundServer server;
    private HttpServer httpsServer;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public SpotifyApiGateway(SoundServer soundServer, String appClientId, String appClientSecret, String appScopes, String appCallbackUrl) {
        this.server = soundServer;
        this.appClientId = appClientId;
        this.appClientSecret = appClientSecret;
        this.appScopes = appScopes.replace(",", "%20");
        this.appCallbackUrl = appCallbackUrl;
        this.authToken = "";
        this.validity = -1;
        this.lastRefresh = -1;
    }


    /**
     * Try to read from config spotify client access token. If such token doesn't exist, nothing'll happen.
     */
    public void setClientCredentials(String authToken, String refreshToken) {
        if (this.refreshToken != null && !this.refreshToken.isBlank()) {
            throw new SecurityException("cannot modify refresh token");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new NullPointerException("refresh token cannot be null");
        }
        this.refreshToken = refreshToken;

        if (this.authToken != null && !this.authToken.isBlank()) {
            throw new SecurityException("cannot modify auth token");
        }
        if (authToken == null || authToken.isBlank()) {
            throw new NullPointerException("auth token cannot be null");
        }
        this.authToken = authToken;
    }

    public boolean isTokenValid() {
        return (lastRefresh + validity) > Instant.now().getEpochSecond();
    }

    /**
     * @return Spotify API url used by user to gain auth code
     */
    public String getAuthUrl() {
        // init http server to save user's time
        if (httpsServer == null && startHttpsServer()) {
            LOGGER.info("Failed to start http(s) server. Token has to be acquired manually.");
        }
        return getAuthUrl(appCallbackUrl);
    }

    public String getAuthUrl(String callbackUrl) {
        return CONST_SPOTIFY_AUTH_ENDPOINT_AUTHORIZE.concat("?client_id=").concat(appClientId)
                .concat("&response_type=code")
                .concat("&redirect_uri=").concat(callbackUrl)
                .concat("&scope=").concat(appScopes);
    }

     /**
     * Exchanges access code to auth token.
     * @param accessCode User access code received from the request to {@link #getAuthUrl()}
     */
    public void exchangeAccessCodeToAuthToken(final String accessCode) {
        makeSpotifyApiRequest(GrantType.AUTH_CODE, String.format("&code=%s&redirect_uri=%s",
                stripQuery(accessCode, true), appCallbackUrl));
    }

    public void refreshToken() {
        LOGGER.info("Refreshing access token...");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalStateException("Refresh token was not set. Have you exchanged access token to auth token?");
        }
        makeSpotifyApiRequest(GrantType.REFRESH, "&refresh_token=" + refreshToken);
    }

    ///////////////////////////////////////////////////////////////////////////
    // HTTP request API
    ///////////////////////////////////////////////////////////////////////////
    // TODO: przetestować
    // raczej not null na pierwszy rzut oka TODO: zweryfikować
    @NotNull
    HttpResponse<String> makeRequest(@NotNull URI uri,
                                     @NotNull String headerArguments,
                                     @NotNull String bodyArguments,
                                     @NotNull RequestMethod requestMethod,
                                     @NotNull AuthMethod authMethod) {
        LOGGER.trace(String.format("Making Spotify API request to %s (arguments: %s; body arguments: %s)", uri, headerArguments, bodyArguments));

        if (!isTokenValid() && authMethod.isAuthTokenRequired()) {
            refreshToken();
        }
        // Has to be after refresh token method call (refresh token might update current token) and if it had happened,
        //  the request would have failed.
        AuthMethod.set(appClientId, appClientSecret, authToken);

        // HTTP refresh request to the spotify api
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", authMethod.authString());

        // Depending on request method, URI has to be created in different ways.
        switch (requestMethod) {
            case POST:
                requestBuilder.uri(uri)
                        .POST(HttpRequest.BodyPublishers.ofString(headerArguments));
                break;
            case GET:
                requestBuilder.uri(URI.create(uri + "?" + headerArguments))
                        .GET();
                break;
            case PUT:
                requestBuilder.uri(URI.create(uri + "?" + headerArguments))
                        .PUT(HttpRequest.BodyPublishers.ofString(bodyArguments));
                break;
            default:
                throw new UnsupportedOperationException("unsupported request method: " + requestMethod);
        }

        HttpRequest request = requestBuilder.build();

        // Send request
        HttpResponse<String> response = null;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            LOGGER.error("Failed to make Spotify API refresh request", e);
        }
        if (response == null) {
            throw new NullPointerException("response cannot be null");
        }
        LOGGER.debug(String.format("Spotify API request returned code %d (response: %s)", response.statusCode(), response.body()));

        return response;
    }

    protected void makeSpotifyApiRequest(final GrantType grantType, final String additionalArguments) {
        // Be sure token doesn't expire a couple of seconds before our calculation result.
        long beforeRefresh = Instant.now().getEpochSecond();
        // POST to the API_ENDPOINT_REFRESH (probably https://accounts.spotify.com/api/token)
        HttpResponse<String> refreshResponse = makeRequest(URI.create(CONST_SPOTIFY_AUTH_ENDPOINT_REFRESH),
                "grant_type=" + grantType + additionalArguments, "", RequestMethod.POST, BASIC);

        // TODO: zweryfikować, czy jakieś inne kody też są poprawne (np. redirect) i jak je obsłużyć
        if (refreshResponse.statusCode() != 200) {
            LOGGER.error("Failed to make Spotify API request");
            LOGGER.error(refreshResponse);
            throw new InvalidRequestStateException("HTTP response code (" + refreshResponse.statusCode() + ") is other than 200! Response from Spotify API: " + refreshResponse.body());
        }

        // Parse response
        JsonObject responseJson = JsonParser.parseString(refreshResponse.body()).getAsJsonObject();
        LOGGER.trace(format("Received JSON: %s", responseJson));

        setAuthToken(responseJson.get("access_token").getAsString());
        if (grantType == GrantType.AUTH_CODE) {
            setRefreshToken(responseJson.get("refresh_token").getAsString());
        }
        // from the api we get validity in seconds
        this.validity = responseJson.get("expires_in").getAsInt();
        this.lastRefresh = beforeRefresh;
        // update config
        server.getConfig().put(ACTION_SPOTIFY_CLIENT_TOKEN_LAST_REFRESH, beforeRefresh);
        server.getConfig().put(ACTION_SPOTIFY_CLIENT_TOKEN_VALIDITY, validity);

        LOGGER.info(format("Access code to auth token successfully exchanged! Validity: %d seconds.", validity));
    }

    private void setAuthToken(final String newToken) {
        this.authToken = newToken;
        server.getConfig().put(ACTION_SPOTIFY_CLIENT_TOKEN, this.authToken);
    }

    private void setRefreshToken(final String newToken) {
        this.refreshToken = newToken;
        server.getConfig().put(ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH, this.refreshToken);
    }

    private boolean startHttpsServer() {
        if (httpsServer != null) {
            throw new IllegalStateException("Https server is not null (was previously started)");
        }
        try {
            httpsServer = HttpServer.create();
        } catch (IOException e) {
            LOGGER.info("Failed to create temporary http server! The process of obtaining an access token won't be automatic.");
            LOGGER.info(e);
            return false;
        }

        int port = Integer.parseInt(server.getConfig().get(SPOTIFY_HTTPS_PORT));
        int backlog = Integer.parseInt(server.getConfig().getOrDefault(SPOTIFY_HTTPS_BACKLOG, "0"));
        String ip = server.getConfig().get(SPOTIFY_HTTPS_IP);
        String hostname = server.getConfig().get(SPOTIFY_HOSTNAME);
        InetSocketAddress inetSAddr = new InetSocketAddress(ip, port);

        // queue only up to 10 incoming tcp connection if server is busy processing requests, other will probably
        //  be ignored
        try {
            httpsServer.bind(inetSAddr, backlog);
        } catch (IOException e) {
            LOGGER.info("Failed to bind temporary http server to address " + ip + ":" + port);
            LOGGER.debug(e);
            return false;
        }

        // configure ssl
        /*try {
            String algo = server.getConfig().getOrDefault(SPOTIFY_HTTPS_ALGORITHM, CONST_SPOTIFY_HTTPS_ALGORITHM_DEFAULT);
            SSLContext sslContext = SSLContext.getInstance(algo);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(CONST_SPOTIFY_HTTPS_KEY_MANAGER_ALGORITHM);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(CONST_SPOTIFY_HTTPS_TRUST_MANAGER_ALGORITHM);
            SecureRandom sr = SecureRandom.getInstance(CONST_SPOTIFY_HTTPS_SECURE_RANDOM_ALGORITHM);

            // TODO: init the factories

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), sr);

//            HttpsConfigurator httpsConf = new HttpsConfigurator(sslContext);
//            httpsConf.configure(HttpsParameters);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.info("Failed to find encryption algorithm specified in config and/or the default algorithm");
            LOGGER.info(e);
            return false;
        }*/
//        HttpsConfigurator httpsConf = new HttpsConfigurator()

        httpsServer.createContext(CONST_SPOTIFY_HTTPS_ROOT_PATH, new CallbackHttpHandler(this));
        LOGGER.info("Starting temporary http(s) server at " + httpsServer.getAddress());
        httpsServer.start();

        return true;
    }

    @Synchronized
    public void stopServer() {
        LOGGER.info("Stopping temporary spotify web server...");
        httpsServer.stop(1);
    }

    enum GrantType {
        REFRESH("refresh_token"),
        AUTH_CODE("authorization_code");

        private final String type;
        GrantType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public enum RequestMethod {
        GET, POST, PUT
    }

    public enum AuthMethod {
        BASIC(false) {
            @Override
            public String authString() {
                return "Basic " + Base64.getEncoder().encodeToString((appClientId + ":" + appClientSecret).getBytes());
            }
        },
        BEARER(true) {
            @Override
            public String authString() {
                return "Bearer " + appAuthToken;
            }
        };

        private static String appClientId;
        private static String appClientSecret;
        private static String appAuthToken;
        @Getter
        private final boolean authTokenRequired;

        AuthMethod(boolean authTokenRequired) {
            this.authTokenRequired = authTokenRequired;
        }

        /**
         * Has to be called before calling any authString methods and has to be refreshed periodically
         */
        public static void set(@NotNull String appClientId, @NotNull String appClientSecret, @NotNull String appAuthToken) {
            AuthMethod.appClientId = appClientId;
            AuthMethod.appClientSecret = appClientSecret;
            AuthMethod.appAuthToken = appAuthToken;

            if (appClientId.isEmpty()) {
                throw new IllegalArgumentException(ACTION_SPOTIFY_APPID + " not set");
            }

            if (appClientSecret.isEmpty()) {
                throw new IllegalArgumentException(ACTION_SPOTIFY_APPSECRET + " not set");
            }
        }

        public abstract String authString();
    }
}
