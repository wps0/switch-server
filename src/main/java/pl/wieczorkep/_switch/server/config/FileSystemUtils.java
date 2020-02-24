package pl.wieczorkep._switch.server.config;

import java.io.*;
import java.nio.file.FileSystemException;
import java.util.*;
import java.util.stream.Collectors;

public final class FileSystemUtils {
    private FileSystemUtils() {}

    public static void initializeConfig(AppConfig appConfig) throws FileSystemException {
        // init
        File configRoot = new File(appConfig.get(AppConfig.CONFIG_DIR));
        File songsDir = new File(appConfig.get(AppConfig.SONGS_DIR));
        File actionsDir = new File(appConfig.get(AppConfig.ACTIONS_DIR));

        File configFile = new File(appConfig.get(AppConfig.CONFIG_FILE));
        File actionsFile = new File(appConfig.get(AppConfig.ACTIONS_FILE));

        boolean status = true;

        // dir presence checks
        if (!configRoot.exists())
            status = configRoot.mkdir();

        if (!songsDir.exists())
            status &= songsDir.mkdir();

        if (!actionsDir.exists())
            status &= actionsDir.mkdir();

        // file presence checks
        try {
            // BEGIN config file
            if (!configFile.exists()) {

                status &= configFile.createNewFile();

                try (BufferedOutputStream configOutputStream = new BufferedOutputStream(new FileOutputStream(configFile))) {
                    AppConfig.getDefaultProperties().store(configOutputStream, "siema to komentarz jest");
                }
            } else {
                loadConfig(appConfig);
            }
            // END config file

            // BEGIN actions file
            if (!actionsFile.exists()) {
                status &= actionsFile.createNewFile();

                try (FileWriter writer = new FileWriter(actionsFile)) {
//                    writer.write("# Includes the execution times of the tasks specified under " + appConfig.get(AppConfig.ACTIONS_DIR) + "\n");
//                    writer.write("# Example:\n");
//                    writer.write("#   ---  fire the actions specified in the break.action at 8.45");
//                    writer.write("# 8.50=lesson");
                    writer.write("# Each action file has to be registered here\n");
                    writer.write("# Example:\n");
                    writer.write("# active=lessons,breaks,weather,szczesliwynumerek");
                    writer.write("# This example registers the files named lessons.action, breaks.actions etc.");
                    writer.write("active=");
                }


            } else {
                loadActions(appConfig);
            }
            // END actions file


        }  catch (IOException e) {
            e.printStackTrace();
            appConfig.getView().error(e.toString());
        }

        // when the creation failed, throw new exception
        if (!status) {
            throw new FileSystemException("Failed to create config files. Check permissions");
        }
    }

    public static void loadConfig(AppConfig appConfig) throws IOException {
        try (BufferedInputStream configInputStream = new BufferedInputStream(new FileInputStream(new File(appConfig.get(AppConfig.CONFIG_FILE))))) {
            appConfig.getProps().load(configInputStream);
        }
    }

    public static void loadActions(AppConfig appConfig) throws IOException {
        List<String> actionFiles;

        try (BufferedInputStream actionsInputStream = new BufferedInputStream(new FileInputStream(new File(appConfig.get(AppConfig.ACTIONS_FILE))))) {
            Properties activeActions = new Properties();
            activeActions.load(actionsInputStream);

            String actions = (String) Optional.ofNullable(activeActions.get("active"))
                    .orElse("");

            actionFiles = Arrays.stream(actions.split(","))
                    .map(s -> s + ".action")
                    .collect(Collectors.toList());
        }
    }
}
