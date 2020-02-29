package pl.wieczorkep._switch.server.utils;

import lombok.Cleanup;
import pl.wieczorkep._switch.server.config.ActionFactory;
import pl.wieczorkep._switch.server.config.AppConfig;

import java.io.*;
import java.nio.file.FileSystemException;

import static pl.wieczorkep._switch.server.utils.ActionUtils.loadActions;

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

                @Cleanup BufferedOutputStream configOutputStream = new BufferedOutputStream(new FileOutputStream(configFile));
                AppConfig.getDefaultProperties().store(configOutputStream, "The main config file.\nRefer to siema for siema");

            } else {
                loadConfig(appConfig);
            }
            // END config file

            // BEGIN example actions file
            File exampleActionsFile = new File(appConfig.get(AppConfig.ACTIONS_DIR) + File.separatorChar + "example.action.example");
            if (!exampleActionsFile.exists()) {
                ActionFactory actionFactory = new ActionFactory();
                actionFactory.createActionFile(appConfig);
            }
            // END example actions file

            // BEGIN actions file
            if (!actionsFile.exists()) {
                status &= actionsFile.createNewFile();

                @Cleanup FileWriter registerWriter = new FileWriter(actionsFile);
//                    writer.write("# Includes the execution times of the tasks specified under " + appConfig.get(AppConfig.ACTIONS_DIR) + "\n");
//                    writer.write("# Example:\n");
//                    writer.write("#   ---  fire the actions specified in the break.action at 8.45");
//                    writer.write("# 8.50=lesson");
                registerWriter.write("# Each action file has to be registered here\n");
                registerWriter.write("# Example:\n");
                registerWriter.write("# active=lessons,breaks,weather,szczesliwynumerek\n");
                registerWriter.write("# This example registers the files named lessons.action, breaks.actions etc.\n");
                registerWriter.write("active=");
            } else {
                loadActions(appConfig);
            }
            // END actions file


        } catch (IOException e) {
            e.printStackTrace();
            appConfig.getView().error(e.toString());
        }

        // when the creation failed, throw new exception
        if (!status) {
            throw new FileSystemException("Failed to create config files. Check permissions");
        }
    }

    public static void loadConfig(AppConfig appConfig) throws IOException {
        @Cleanup BufferedInputStream configInputStream = new BufferedInputStream(new FileInputStream(appConfig.get(AppConfig.CONFIG_FILE)));
        appConfig.getProps().load(configInputStream);
    }
}
