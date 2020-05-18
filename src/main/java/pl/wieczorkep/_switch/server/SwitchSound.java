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
        concurrencyManager.init();

        spotifyApiGateway = new SpotifyApiGateway("64f78c9a8a51413a86364dd9970dabb6",
                "15c49b6ace0f421d94c73f59754e0018", "user-read-playback-state,user-modify-playback-state",
                "http://localhost/");
    }

    public static AppConfig getConfig() {
        return config;
    }
}
