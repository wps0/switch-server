package pl.wieczorkep._switch.server;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.core.concurrent.ConcurrencyManager;
import pl.wieczorkep._switch.server.core.utils.ConfigUtils;
import pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway;
import pl.wieczorkep._switch.server.integration.spotify.SpotifyMacros;

import java.nio.file.FileSystemException;

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
        LOGGER.info(SpotifyMacros.Player.startPlayback(spotifyApiGateway, "85a04d078a7a84537c9a1bfa6b3866431fd9deb3", "", 0, 0));
        concurrencyManager.init(this);
    }

    private void initConfig() throws FileSystemException {
        config.init();
        ConfigUtils.initializeConfig(this);
    }

    private void initSpotifyIntegration() {
        // TODO: add www server for token obtaining purposes
        spotifyApiGateway = new SpotifyApiGateway(this, config.get(ACTION_SPOTIFY_APPID),
                config.get(ACTION_SPOTIFY_APPSECRET), config.get(ACTION_SPOTIFY_AUTHSCOPES),
                "http://localhost:4144/callback");

        // init spotify status variables
        String validity = config.get(ACTION_SPOTIFY_CLIENT_TOKEN_VALIDITY);
        if (validity != null && !validity.isEmpty()) {
            spotifyApiGateway.setValidity(Integer.parseInt(validity));
        }
        String lastRefresh = config.get(ACTION_SPOTIFY_CLIENT_TOKEN_LAST_REFRESH);
        if (lastRefresh != null && !lastRefresh.isEmpty()) {
            spotifyApiGateway.setValidity(Integer.parseInt(lastRefresh));
        }

        LOGGER.info(spotifyApiGateway.getAuthUrl());
        spotifyApiGateway.setClientCredentials(config.get(ACTION_SPOTIFY_CLIENT_TOKEN), config.get(ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH));

        String clientTmpCode = config.get(ACTION_SPOTIFY_CLIENT_TMPCODE);
        if (clientTmpCode != null && !clientTmpCode.isEmpty()) {
            spotifyApiGateway.exchangeAccessCodeToAuthToken(clientTmpCode);
            config.put(ACTION_SPOTIFY_CLIENT_TMPCODE, "");
        }
    }
}