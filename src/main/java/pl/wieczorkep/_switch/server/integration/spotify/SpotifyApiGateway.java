package pl.wieczorkep._switch.server.integration.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import pl.wieczorkep._switch.server.SoundServer;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.Base64;

import static java.lang.String.format;
import static pl.wieczorkep._switch.server.Constants.*;
import static pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway.AuthMethod.BASIC;

@Log4j2
public class SpotifyApiGateway {
    private final String appClientId;
    private final String appClientSecret;
    private final String appScopes;
    private final String appCallbackUrl;
    private final SoundServer server;
    private String authToken;
    private String refreshToken;
    @Getter @Setter
    private int validity;
    @Getter @Setter
    private long lastRefresh;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public SpotifyApiGateway(SoundServer soundServer, String appClientId, String appClientSecret, String appScopes, String appCallbackUrl) {
        this.server = soundServer;
        this.appClientId = appClientId;
        this.appClientSecret = appClientSecret;
        this.appScopes = appScopes.replace(",", "%20");
        this.appCallbackUrl = appCallbackUrl;
        this.validity = -1;
        this.lastRefresh = -1;
    }


    /**
     * Try to read from config spotify client access token. If such token doesn't exist, nothing'll happen.
     */
    public void setClientCredentials(String authToken, String refreshToken) {
        if (this.refreshToken != null && !this.refreshToken.isEmpty()) {
            throw new SecurityException("cannot modify refresh token");
        }
        this.refreshToken = refreshToken;

        if (this.authToken != null && !this.authToken.isEmpty()) {
            throw new SecurityException("cannot modify auth token");
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
        return SPOTIFY_AUTH_ENDPOINT_AUTHORIZE.concat("?client_id=").concat(appClientId)
                .concat("&response_type=code")
                .concat("&redirect_uri=").concat(appCallbackUrl)
                .concat("&scope=").concat(appScopes);
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
            if (response == null) {
                throw new NullPointerException("response cannot be null");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to make Spotify API refresh request", e);
        }
        LOGGER.debug(String.format("Spotify API request returned code %d (response: %s)", response.statusCode(), response.body()));

        return response;
    }

    protected void makeSpotifyApiRequest(final GrantType grantType, final String additionalArguments) {
        // POST to the API_ENDPOINT_REFRESH (probably https://accounts.spotify.com/api/token)
        HttpResponse<String> refreshResponse = makeRequest(URI.create(SPOTIFY_AUTH_ENDPOINT_REFRESH),
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

        // TODO: czy to tak zadziała?
        setAuthToken(responseJson.get("access_token").getAsString());
        if (grantType == GrantType.AUTH_CODE) {
            setRefreshToken(responseJson.get("refresh_token").getAsString());
        }
        // from the api we get validity in seconds
        this.validity = responseJson.get("expires_in").getAsInt();
        this.lastRefresh = Instant.now().getEpochSecond();
        // update config
        server.getConfig().put(ACTION_SPOTIFY_CLIENT_TOKEN_LAST_REFRESH, lastRefresh);
        server.getConfig().put(ACTION_SPOTIFY_CLIENT_TOKEN_VALIDITY, validity);

        LOGGER.info(format("Access code to auth token successfully exchanged! Validity: %d seconds.", validity));
    }

    private void setAuthToken(final String newToken) {
        this.authToken = newToken;
        server.getConfig().put(ACTION_SPOTIFY_CLIENT_TOKEN, newToken);
    }

    private void setRefreshToken(final String newToken) {
        this.refreshToken = newToken;
        server.getConfig().put(ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH, newToken);
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
