package pl.wieczorkep._switch.server;

import pl.wieczorkep._switch.server.concurrent.ConcurrencyManager;
import pl.wieczorkep._switch.server.config.AppConfig;
import pl.wieczorkep._switch.server.utils.FileSystemUtils;
import pl.wieczorkep._switch.server.view.ConsoleView;

import java.nio.file.FileSystemException;
import java.util.Arrays;

public class SwitchSound {
    private static AppConfig config = new AppConfig(new ConsoleView());
    private static ConcurrencyManager concurrencyManager = new ConcurrencyManager(config);

    // ToDo: cmd arguments handling
    public static void main(String[] args) {
        config.init();
        try {
            FileSystemUtils.initializeConfig(config);
        } catch (FileSystemException e) {
            config.getView().error(e.getMessage());
            config.getView().error(Arrays.toString(e.getStackTrace()));
        }
        concurrencyManager.init();
    }

    public static AppConfig getConfig() {
        return config;
    }
}
