package pl.wieczorkep._switch.server;

import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.core.concurrent.ConcurrencyManager;
import pl.wieczorkep._switch.server.core.utils.ConfigUtils;
import pl.wieczorkep._switch.server.spotify_api.SpotifyApiGateway;
import pl.wieczorkep._switch.server.spotify_api.SpotifyMacros;
import pl.wieczorkep._switch.server.view.ConsoleView;

import java.nio.file.FileSystemException;
import java.util.Arrays;

public class SwitchSound {
    private static AppConfig config = new AppConfig(new ConsoleView());
    @Getter
    private static ConcurrencyManager concurrencyManager = new ConcurrencyManager(config);
    @Getter
    private static SpotifyApiGateway spotifyApiGateway;

    // ToDo: cmd arguments handling
    public static void main(String[] args) {
        // set logger level to Level.ALL for debug purposes
        Configurator.setRootLevel(Level.ALL);
        config.init();
        try {
            ConfigUtils.initializeConfig(config);
        } catch (FileSystemException e) {
            config.getView().error(e.getMessage());
            config.getView().error(Arrays.toString(e.getStackTrace()));
        }

        spotifyApiGateway = new SpotifyApiGateway(config.get(AppConfig.ACTION_SPOTIFY_APPID),
                config.get(AppConfig.ACTION_SPOTIFY_APPSECRET), config.get(AppConfig.ACTION_SPOTIFY_AUTHSCOPES),
                "http://localhost/");

        System.out.println(spotifyApiGateway.getAuthUrl());

        spotifyApiGateway.setClientCredentials(config.get(AppConfig.ACTION_SPOTIFY_CLIENT_TOKEN), config.get(AppConfig.ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH));

        if (config.get(AppConfig.ACTION_SPOTIFY_CLIENT_TMPCODE) != null && !config.get(AppConfig.ACTION_SPOTIFY_CLIENT_TMPCODE).isEmpty()) {
            spotifyApiGateway.exchangeAccessCodeToAuthToken(config.get(AppConfig.ACTION_SPOTIFY_CLIENT_TMPCODE));
            config.put(AppConfig.ACTION_SPOTIFY_CLIENT_TMPCODE, "");
        }

        System.out.println(SpotifyMacros.Player.getAvailableDevices(spotifyApiGateway));
        System.out.println(SpotifyMacros.Player.getPlaybackInfo(spotifyApiGateway));

        concurrencyManager.init();
    }

    public static AppConfig getConfig() {
        return config;
    }
}
