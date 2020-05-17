package pl.wieczorkep._switch.server.core;

import javax.management.RuntimeErrorException;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.Base64;

public class SpotifyApiGateway {
    public static final String API_ENDPOINT = "https://accounts.spotify.com";
    public static final String API_ENDPOINT_AUTH = API_ENDPOINT + "/authorize";
    public static final String API_ENDPOINT_REFRESH = API_ENDPOINT + "/api/token";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private final String APP_CLIENT_ID;
    private final String APP_CLIENT_SECRET;
    private final String APP_SCOPES;
    private final String APP_CALLBACKURL;

    public SpotifyApiGateway(String appClientId, String appClientSecret, String appScopes, String appCallbackUrl) {
        APP_CLIENT_ID = appClientId;
        APP_CLIENT_SECRET = appClientSecret;
        APP_SCOPES = appScopes.replace(",", "%20");
        APP_CALLBACKURL = appCallbackUrl;
    }

    public String getAuthUrl() {
        return API_ENDPOINT_AUTH.concat("?client_id=").concat(APP_CLIENT_ID)
                .concat("&response_type=code")
                .concat("&redirect_uri=").concat(APP_CALLBACKURL)
                .concat("&scope=").concat(APP_SCOPES);
    }

    public void exchangeAccessCodeToAuthToken(final String accessCode) {
        // POST to the API_ENDPOINT_REFRESH (probably https://accounts.spotify.com/api/token)
        final String refreshRequestParams = "grant_type=authorization_code&code=" + accessCode +
                "&redirect_uri=" + APP_CALLBACKURL;
        HttpRequest refreshRequest = HttpRequest.newBuilder(URI.create(API_ENDPOINT_REFRESH))
                .version(HttpClient.Version.HTTP_2)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString((APP_CLIENT_ID + ":" + APP_CLIENT_SECRET).getBytes()))
                .POST(HttpRequest.BodyPublishers.ofString(refreshRequestParams))
                .build();

        HttpResponse<String> refreshResponse = null;
        try {
            refreshResponse = HTTP_CLIENT.send(refreshRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            AppConfig.getLogger().warn("Failed to make Spotify API refresh request", e);
        }
        AppConfig.getLogger().info("Spotify API request returned code " + refreshResponse.statusCode());

        if (refreshResponse.statusCode() != 200) {
            AppConfig.getLogger().warn("Failed to make Spotify API request");
            throw new RuntimeErrorException(new Error("HTTP response code is other than 200! Response from Spotify API: " + refreshResponse.body()));
        }
    }

    public void refreshToken() {

    }

    public static void main(String[] args) {
        SpotifyApiGateway apiGateway = new SpotifyApiGateway("64f78c9a8a51413a86364dd9970dabb6",
                "15c49b6ace0f421d94c73f59754e0018", "user-read-playback-state,user-modify-playback-state",
                "http://localhost/");
        System.out.println(apiGateway.getAuthUrl());
        apiGateway.exchangeAccessCodeToAuthToken("");
    }
}
