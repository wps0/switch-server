package pl.wieczorkep._switch.server.core.utils;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.SoundServer;
import pl.wieczorkep._switch.server.core.AppConfig;

import java.io.*;
import java.nio.file.FileSystemException;
import java.util.Properties;

import static pl.wieczorkep._switch.server.Constants.*;
import static pl.wieczorkep._switch.server.core.utils.ActionLoader.loadActions;
import static pl.wieczorkep._switch.server.core.utils.FileSystemUtils.createSpotifyActionFile;

@Log4j2
public final class ConfigUtils {
    private ConfigUtils() {}

    public static void initializeConfig(SoundServer soundServer) throws FileSystemException {
        // init
        File configRoot = new File(soundServer.getConfig().get(CONFIG_DIR));
        File songsDir = new File(soundServer.getConfig().get(SONGS_DIR));
        File actionsDir = new File(soundServer.getConfig().get(ACTIONS_DIR));

        File configFile = new File(soundServer.getConfig().get(CONFIG_FILE));
        File actionsFile = new File(soundServer.getConfig().get(ACTIONS_FILE));

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

                @Cleanup
                BufferedOutputStream configOutputStream = new BufferedOutputStream(new FileOutputStream(configFile));
                getDefaultConfig().store(configOutputStream, "The main config file.\nRefer to siema for siema");

            } else {
                loadConfig(soundServer.getConfig());
            }
            // END config file

            // BEGIN example actions file
            File exampleActionsFile = new File(soundServer.getConfig().get(ACTIONS_DIR) + File.separatorChar + "action.example");
            if (!exampleActionsFile.exists()) {
                ActionFactory actionFactory = new ActionFactory();
                actionFactory.createActionFile(soundServer.getConfig());
            }
            File exampleSpotifyActionFile = new File(soundServer.getConfig().get(ACTIONS_DIR) + File.separatorChar + "spotify-action.example");
            if (!exampleSpotifyActionFile.exists()) {
                createSpotifyActionFile(ActionFactory.createExampleSpotifyAction(), new File(soundServer.getConfig().get(ACTIONS_DIR)));
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
                loadActions(soundServer.getConfig());
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

    public static Properties getDefaultConfig() {
        Properties defaultProperties = new Properties();
        // --- Storage ---
        defaultProperties.setProperty(CONFIG_DIR, System.getProperty("user.home") + File.separatorChar + "SwitchSoundServer");
        defaultProperties.setProperty(SONGS_DIR, defaultProperties.getProperty(CONFIG_DIR) + File.separatorChar + "sounds");
        defaultProperties.setProperty(ACTIONS_DIR, defaultProperties.getProperty(CONFIG_DIR) + File.separatorChar + "actions");

        // -- Specific files --
        defaultProperties.setProperty(CONFIG_FILE, defaultProperties.getProperty(CONFIG_DIR) + File.separatorChar + "config.props");
        defaultProperties.setProperty(ACTIONS_FILE, defaultProperties.getProperty(ACTIONS_DIR) + File.separatorChar + "actions.props");

        // --- Spotify properties ---
        defaultProperties.setProperty(ACTION_SPOTIFY_APPID, "64f78c9a8a51413a86364dd9970dabb6");
        defaultProperties.setProperty(ACTION_SPOTIFY_APPSECRET, "");
        defaultProperties.setProperty(ACTION_SPOTIFY_AUTHSCOPES, "user-read-playback-state,user-modify-playback-state," +
                "playlist-read-collaborative,user-read-playback-position,user-read-currently-playing," +
                "playlist-read-private,app-remote-control");
        defaultProperties.setProperty(ACTION_SPOTIFY_CLIENT_TOKEN, "");
        defaultProperties.setProperty(ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH, "");
        defaultProperties.setProperty(ACTION_SPOTIFY_CLIENT_TMPCODE, "");
        defaultProperties.setProperty(ACTION_SPOTIFY_CLIENT_DEFAULTDEVICE, "");
        defaultProperties.setProperty(SPOTIFY_HTTPS_PORT, "4144");
        defaultProperties.setProperty(SPOTIFY_HTTPS_IP, "0.0.0.0");
        defaultProperties.setProperty(SPOTIFY_HTTPS_HOSTNAME, "rpi.switch.zsi.kielce.pl");
        defaultProperties.setProperty(SPOTIFY_HTTPS_ALGORITHM, "TLSv1.2");
        return defaultProperties;
    }
}
