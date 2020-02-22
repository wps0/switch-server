package pl.wieczorkep._switch.server.config;

import java.io.*;
import java.nio.file.FileSystemException;

public class FileSystemUtils {
    private FileSystemUtils() {}

    public static void initializeConfig(AppConfig appConfig) throws FileSystemException {
        File configRoot = new File(appConfig.get(AppConfig.CONFIG_DIR));
        File songsDir = new File(appConfig.get(AppConfig.SONGS_DIR));
        File configFile = new File(appConfig.get(AppConfig.CONFIG_FILE));

        boolean status = true;

        if (!configRoot.exists())
            status = configRoot.mkdir();

        if (!songsDir.exists())
            status &= songsDir.mkdir();

        try {
            if (!configFile.exists()) {

                status &= configFile.createNewFile();

                try (BufferedOutputStream configOutputStream = new BufferedOutputStream(new FileOutputStream(configFile))) {
                    AppConfig.getDefaultProperties().store(configOutputStream, "siema to komentarz jest");
                }
            } else {
                loadConfig(appConfig);
            }

        }  catch (IOException e) {
            e.printStackTrace();
            appConfig.getView().error(e.toString());
        }

        if (!status) {
            throw new FileSystemException("Failed to create config files. Check permissions");
        }

    }

    public static void loadConfig(AppConfig appConfig) throws IOException {
        try (BufferedInputStream configInputStream = new BufferedInputStream(new FileInputStream(new File(appConfig.get(AppConfig.CONFIG_FILE))))) {
            appConfig.getProps().load(configInputStream);
        }
    }
}
