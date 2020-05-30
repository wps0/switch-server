package pl.wieczorkep._switch.server.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import pl.wieczorkep._switch.server.SwitchSound;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.Base64;

import static java.lang.String.format;
import static pl.wieczorkep._switch.server.core.SpotifyApiGateway.AuthMethod.*;

public class SpotifyApiGateway {
    public static final String AUTH_ENDPOINT = "https://accounts.spotify.com";
    public static final String API_ENDPOINT = "https://api.spotify.com/v1/";
    public static final String AUTH_ENDPOINT_AUTHORIZE = AUTH_ENDPOINT + "/authorize";
    public static final String AUTH_ENDPOINT_REFRESH = AUTH_ENDPOINT + "/api/token";

    private final String APP_CLIENT_ID;
    private final String APP_CLIENT_SECRET;
    private final String APP_SCOPES;
    private final String APP_CALLBACKURL;

    private static final Logger LOGGER = AppConfig.getLogger(SpotifyApiGateway.class);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private String authToken;
    private String refreshToken;
    private int validity;
    private long lastRefresh;

    public SpotifyApiGateway(String appClientId, String appClientSecret, String appScopes, String appCallbackUrl) {
        APP_CLIENT_ID = appClientId;
        APP_CLIENT_SECRET = appClientSecret;
        APP_SCOPES = appScopes.replace(",", "%20");
        APP_CALLBACKURL = appCallbackUrl;
        this.validity = -1;
        this.lastRefresh = -1;
    }


    /**
     * Try to read from config spotify client access token. If such token doesn't exist, nothing'll happen.
     */
    public void setClientCredentials(String authToken, String refreshToken) {
        if (this.refreshToken == null || this.refreshToken.isEmpty()) {
            this.refreshToken = refreshToken;
        } else {
            throw new SecurityException("cannot modify refresh token");
        }

        if (this.authToken == null || !this.authToken.isEmpty()) {
            this.authToken = authToken;
        } else {
            throw new SecurityException("cannot modify auth token");
        }
    }

    public boolean isTokenValid() {
        return (lastRefresh + validity) > Instant.now().getEpochSecond();
    }

    /**
     * @return Spotify API url used by user to gain auth code
     */
    public String getAuthUrl() {
        return AUTH_ENDPOINT_AUTHORIZE.concat("?client_id=").concat(APP_CLIENT_ID)
                .concat("&response_type=code")
                .concat("&redirect_uri=").concat(APP_CALLBACKURL)
                .concat("&scope=").concat(APP_SCOPES);
    }

    /**
     * Exchanges access code to auth token.
     * @param accessCode User access code received from the request to {@link #getAuthUrl()}
     */
    public void exchangeAccessCodeToAuthToken(final String accessCode) {
        makeSpotifyApiRequest(GrantType.AUTH_CODE,  "&code=" + accessCode + "&redirect_uri=http://localhost/");
    }

    public void refreshToken() {
        LOGGER.info("Refreshing access token...");
        if (refreshToken == null) {
            throw new IllegalStateException("Refresh token was not set. Have you exchanged access token to auth token?");
        }
        makeSpotifyApiRequest(GrantType.REFRESH, "&refresh_token=" + refreshToken);
    }

    ///////////////////////////////////////////////////////////////////////////
    // HTTP request methods
    ///////////////////////////////////////////////////////////////////////////
    public String getUsersAvailableDevices() {
        return makeRequest(URI.create(API_ENDPOINT + "me/player/devices"), "", RequestMethod.GET, BEARER).body();
    }

    ///////////////////////////////////////////////////////////////////////////
    // HTTP request API
    ///////////////////////////////////////////////////////////////////////////
    // TODO: przetestować
    private HttpResponse<String> makeRequest(@NotNull URI uri,
                                             String arguments,
                                             @NotNull RequestMethod requestMethod,
                                             @NotNull AuthMethod authMethod) {
        authMethod.set(APP_CLIENT_ID, APP_CLIENT_SECRET, authToken);
        LOGGER.trace(String.format("Making Spotify API request to %s (arguments: %s)", uri, arguments));

        if (!isTokenValid() && authMethod.isAuthTokenRequired()) {
            refreshToken();
        }

        // Depending on request method, URI has to be created in different ways.
        URI requestUri;
        if (requestMethod == RequestMethod.GET) {
            requestUri = URI.create(uri + arguments);
        } else {
            requestUri = uri;
        }

        // HTTP refresh request to the spotify api
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(requestUri)
                .version(HttpClient.Version.HTTP_2)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", authMethod.authString());

        switch (requestMethod) {
            case POST:
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(arguments));
                break;
            case GET:
                requestBuilder.GET();
                break;
            default:
                throw new UnsupportedOperationException("unsupported request method: " + requestMethod);
        }

        HttpRequest request = requestBuilder.build();

        // Send request
        HttpResponse<String> response = null;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to make Spotify API refresh request", e);
        }

        LOGGER.debug(String.format("Spotify API request returned code %d", response.statusCode()));

        return response;
    }

    private void makeSpotifyApiRequest(final GrantType grantType, final String additionalArguments) {
        // POST to the API_ENDPOINT_REFRESH (probably https://accounts.spotify.com/api/token)
        HttpResponse<String> refreshResponse = makeRequest(URI.create(AUTH_ENDPOINT_REFRESH), "grant_type=" + grantType + additionalArguments, RequestMethod.POST, BASIC);

        // TODO: zweryfikować, czy jakieś inne kody też są poprawne (np. redirect) i jak je obsłużyć
        if (refreshResponse.statusCode() != 200) {
            LOGGER.error("Failed to make Spotify API request");
            LOGGER.error(refreshResponse);
            throw new RuntimeException("HTTP response code (" + refreshResponse.statusCode() + ") is other than 200! Response from Spotify API: " + refreshResponse.body());
        }

        // Parse response
        JsonObject responseJson = JsonParser.parseString(refreshResponse.body()).getAsJsonObject();
        LOGGER.trace(format("Received JSON: %s", responseJson));

        // TODO: czy to tak zadziała?
        setAuthToken(responseJson.get("access_token").getAsString());
        if (grantType == GrantType.AUTH_CODE) {
            setRefreshToken(responseJson.get("refresh_token").getAsString());
        }
        this.validity = responseJson.get("expires_in").getAsInt();
        this.lastRefresh = Instant.now().getEpochSecond();

        LOGGER.info(format("Access code to auth token successfully exchanged! Validity: %d.", validity));
    }

    private void setAuthToken(final String newToken) {
        this.authToken = newToken;
        SwitchSound.getConfig().put(AppConfig.ACTION_SPOTIFY_CLIENT_TOKEN, newToken);
    }

    private void setRefreshToken(final String newToken) {
        this.refreshToken = newToken;
        SwitchSound.getConfig().put(AppConfig.ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH, newToken);
    }

    enum GrantType {
        REFRESH("refresh_token"),
        AUTH_CODE("authorization_code");

        private String type;
        GrantType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public enum RequestMethod {
        GET, POST
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
         * Has to be called before calling any authString methods and refreshed periodically
         */
        public void set(@NotNull String appClientId, @NotNull String appClientSecret, @NotNull String appAuthToken) {
            AuthMethod.appClientId = appClientId;
            AuthMethod.appClientSecret = appClientSecret;
            AuthMethod.appAuthToken = appAuthToken;

            if (appClientId.isEmpty()) {
                throw new IllegalArgumentException(AppConfig.ACTION_SPOTIFY_APPID + " not set");
            }

            if (appClientSecret.isEmpty()) {
                throw new IllegalArgumentException(AppConfig.ACTION_SPOTIFY_APPSECRET + " not set");
            }
        }

        public abstract String authString();
    }
}
