package pl.wieczorkep._switch.server.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.Base64;

import static java.lang.String.format;

public class SpotifyApiGateway {
    public static final String API_ENDPOINT = "https://accounts.spotify.com";
    public static final String API_ENDPOINT_AUTH = API_ENDPOINT + "/authorize";
    public static final String API_ENDPOINT_REFRESH = API_ENDPOINT + "/api/token";

    private final String APP_CLIENT_ID;
    private final String APP_CLIENT_SECRET;
    private final String APP_SCOPES;
    private final String APP_CALLBACKURL;

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
    }

    public boolean isTokenValid() {
        return (lastRefresh + validity) > Instant.now().getEpochSecond();
    }

    /**
     * @return Spotify API url used by user to gain auth code
     */
    public String getAuthUrl() {
        return API_ENDPOINT_AUTH.concat("?client_id=").concat(APP_CLIENT_ID)
                .concat("&response_type=code")
                .concat("&redirect_uri=").concat(APP_CALLBACKURL)
                .concat("&scope=").concat(APP_SCOPES);
    }

    /**
     * Exchanges access code to auth token.
     * @param accessCode User access code received from the request to {@link #getAuthUrl()}
     */
    public void exchangeAccessCodeToAuthToken(final String accessCode) {
        makeSpotifyApiRequest("authorization_code",  "&code=" + accessCode + "&redirect_uri=http://localhost/");
    }

    public void refreshToken() {
        if (refreshToken == null) {
            throw new IllegalStateException("Refresh token was not set. Have you exchanged access token to auth token?");
        }
        makeSpotifyApiRequest("refresh_token", "&refresh_token=" + refreshToken);
    }

    private void makeSpotifyApiRequest(final String grantType, final String additionalArguments) {
        // POST to the API_ENDPOINT_REFRESH (probably https://accounts.spotify.com/api/token)
        final String refreshRequestParams = "grant_type=" + grantType + additionalArguments;
        // HTTP refresh request to the spotify api
        HttpRequest refreshRequest = HttpRequest.newBuilder(URI.create(API_ENDPOINT_REFRESH))
                .version(HttpClient.Version.HTTP_2)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString((APP_CLIENT_ID + ":" + APP_CLIENT_SECRET).getBytes()))
                .POST(HttpRequest.BodyPublishers.ofString(refreshRequestParams))
                .build();

        // Send request
        HttpResponse<String> refreshResponse = null;
        try {
            refreshResponse = HTTP_CLIENT.send(refreshRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            AppConfig.getLogger().error("Failed to make Spotify API refresh request", e);
        }
        AppConfig.getLogger().info("Spotify API request returned code " + refreshResponse.statusCode());

        if (refreshResponse.statusCode() != 200) {
            AppConfig.getLogger().error("Failed to make Spotify API request");
            throw new RuntimeException("HTTP response code (" + refreshResponse.statusCode() + ") is other than 200! Response from Spotify API: " + refreshResponse.body());
        }

        // Parse response
        JsonObject responseJson = JsonParser.parseString(refreshResponse.body()).getAsJsonObject();
        AppConfig.getLogger().trace(format("Received JSON: %s", responseJson));

        this.authToken = responseJson.get("access_token").getAsString();
        if (grantType.equalsIgnoreCase("authorization_code")) {
            this.refreshToken = responseJson.get("refresh_token").getAsString();
        }
        this.validity = responseJson.get("expires_in").getAsInt();
        this.lastRefresh = Instant.now().getEpochSecond();

        AppConfig.getLogger().info(format("Access code to auth token successfully exchanged! Validity: %d.", validity));
    }
}
