package pl.wieczorkep._switch.server;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.core.concurrent.ConcurrencyManager;
import pl.wieczorkep._switch.server.core.utils.ConfigUtils;
import pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway;

import java.io.IOException;

import static pl.wieczorkep._switch.server.Constants.*;

@RequiredArgsConstructor
@Log4j2
public class SoundServer {
    @Getter @NonNull
    private final AppConfig config;
    @Getter @NonNull
    private final ConcurrencyManager concurrencyManager;
    @Getter
    private SpotifyApiGateway spotifyApiGateway;

    @SneakyThrows
    public void init() {
        initConfig();
        initSpotifyIntegration();
    }

    public void run() {
        concurrencyManager.init(this);
    }

    private void initConfig() throws IOException {
        LOGGER.info("Initializing config utils...");
        config.init();
        ConfigUtils.initializeConfig(this);
    }

    private void initSpotifyIntegration() {
        LOGGER.info("Initializing spotify integration...");

        String appSecret = config.get(ACTION_SPOTIFY_APPSECRET);
        String appId = config.get(ACTION_SPOTIFY_APPID);
        String authScopes = config.get(ACTION_SPOTIFY_AUTHSCOPES);
        if (appSecret == null || appSecret.isBlank()) {
            throw new IllegalArgumentException("spotify app secret cannot be blank");
        }
        if (appId == null || appId.isBlank()) {
            throw new IllegalArgumentException("spotify app id cannot be blank");
        }
        if (authScopes == null || authScopes.isBlank()) {
            throw new IllegalArgumentException("spotify auth scopes cannot be blank");
        }

        String callbackUrl = String.format("http://%s:%d/callback", config.get(SPOTIFY_HOSTNAME), Integer.parseInt(config.get(SPOTIFY_HTTPS_PORT)));
        spotifyApiGateway = new SpotifyApiGateway(this, appId, appSecret, authScopes, callbackUrl);

        // init spotify status variables
        String validity = config.get(ACTION_SPOTIFY_CLIENT_TOKEN_VALIDITY);
        if (validity != null && !validity.isEmpty()) {
            spotifyApiGateway.setValidity(Integer.parseInt(validity));
        }
        String lastRefresh = config.get(ACTION_SPOTIFY_CLIENT_TOKEN_LAST_REFRESH);
        if (lastRefresh != null && !lastRefresh.isEmpty()) {
            spotifyApiGateway.setValidity(Integer.parseInt(lastRefresh));
        }

        try {
            spotifyApiGateway.setClientCredentials(config.get(ACTION_SPOTIFY_CLIENT_TOKEN), config.get(ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH));
        } catch (NullPointerException e) {
            LOGGER.warn("Either spotify client token or refresh token are missing");
            LOGGER.info("You can obtain the tokens at " + spotifyApiGateway.getAuthUrl());
        }

        String clientTmpCode = config.get(ACTION_SPOTIFY_CLIENT_TMPCODE);
        if (clientTmpCode != null && !clientTmpCode.isEmpty()) {
            spotifyApiGateway.exchangeAccessCodeToAuthToken(clientTmpCode);
            config.put(ACTION_SPOTIFY_CLIENT_TMPCODE, "");
        }
        LOGGER.info("Spotify integration initialized.");
    }
}