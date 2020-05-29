package pl.wieczorkep._switch.server;

import lombok.Getter;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.core.SpotifyApiGateway;
import pl.wieczorkep._switch.server.core.concurrent.ConcurrencyManager;
import pl.wieczorkep._switch.server.core.utils.ConfigUtils;
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
        config.init();
        try {
            ConfigUtils.initializeConfig(config);
        } catch (FileSystemException e) {
            config.getView().error(e.getMessage());
            config.getView().error(Arrays.toString(e.getStackTrace()));
        }
        spotifyApiGateway = new SpotifyApiGateway(config.get(AppConfig.ACTION_SPOTIFY_APPID),
                config.get(AppConfig.ACTION_SPOTIFY_APPSECRET), "user-read-playback-state,user-modify-playback-state",
                "http://localhost/");

        spotifyApiGateway.setClientCredentials(config.get(AppConfig.ACTION_SPOTIFY_CLIENT_REFRESHTOKEN), config.get(AppConfig.ACTION_SPOTIFY_CLIENT_TOKEN));

        concurrencyManager.init();
    }

    public static AppConfig getConfig() {
        return config;
    }
}
