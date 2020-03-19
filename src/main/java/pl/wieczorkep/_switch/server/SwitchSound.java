package pl.wieczorkep._switch.server;

import pl.wieczorkep._switch.server.concurrent.ConcurrencyManager;
import pl.wieczorkep._switch.server.config.AppConfig;
import pl.wieczorkep._switch.server.utils.FileSystemUtils;
import pl.wieczorkep._switch.server.view.ConsoleView;

import java.nio.file.FileSystemException;

public class SwitchSound {
    private static AppConfig config = new AppConfig(new ConsoleView());
    private static ConcurrencyManager concurrencyManager = new ConcurrencyManager(config);

    // ToDo: cmd arguments handling
    public static void main(String[] args) {
        config.init();
        try {
            FileSystemUtils.initializeConfig(config);
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        concurrencyManager.init();

        int code = 0;
        while (code != -1) {
            code = config.getView().readInt("?");
            if (code == 1) {

                config.oks();
            }
        }


//        SoundController.main(args);


    }

    public static AppConfig getConfig() {
        return config;
    }
}
