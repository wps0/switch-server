package pl.wieczorkep._switch.server.core.utils;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.Server;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.core.utils.factory.ActionFactory;

import java.io.*;
import java.nio.file.FileSystemException;

import static pl.wieczorkep._switch.server.Constants.*;
import static pl.wieczorkep._switch.server.core.utils.ActionLoader.loadActions;

@Log4j2
public final class ConfigUtils {
    private ConfigUtils() {}

    public static void initializeConfig(Server server) throws FileSystemException {
        // init
        File configRoot = new File(server.getConfig().get(CONFIG_DIR));
        File songsDir = new File(server.getConfig().get(SONGS_DIR));
        File actionsDir = new File(server.getConfig().get(ACTIONS_DIR));

        File configFile = new File(server.getConfig().get(CONFIG_FILE));
        File actionsFile = new File(server.getConfig().get(ACTIONS_FILE));

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
                loadConfig(server.getConfig());
            }
            // END config file

            // BEGIN example actions file
            File exampleActionsFile = new File(server.getConfig().get(ACTIONS_DIR) + File.separatorChar + "example.action.example");
            if (!exampleActionsFile.exists()) {
                ActionFactory actionFactory = new ActionFactory();
                actionFactory.createActionFile(server.getConfig());
            }
            // END example actions file

            // BEGIN actions file
            if (!actionsFile.exists()) {
                status &= actionsFile.createNewFile();

                @Cleanup FileWriter registerWriter = new FileWriter(actionsFile);
                registerWriter.write("# Each action file has to be registered here\n");
                registerWriter.write("# Example:\n");
                registerWriter.write("# active=lessons,breaks,weather,szczesliwynumerek\n");
                registerWriter.write("# This example registers the files named lessons.action, breaks.actions etc.\n");
                registerWriter.write("active=");
            } else {
                loadActions(server.getConfig());
            }
            // END actions file


        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }

        // when the creation failed, throw new exception
        if (!status) {
            throw new FileSystemException("Failed to create config files. Check permissions");
        }
    }

    public static void loadConfig(AppConfig appConfig) throws IOException {
        @Cleanup BufferedInputStream configInputStream = new BufferedInputStream(new FileInputStream(appConfig.get(CONFIG_FILE)));
        appConfig.getProps().load(configInputStream);
    }
}
