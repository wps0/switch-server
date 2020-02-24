package pl.wieczorkep._switch.server.config;

import pl.wieczorkep._switch.server.view.View;

import java.io.File;
import java.util.Properties;

public class AppConfig {
    private View view;
    private Properties props;
    private Properties actions;

    public static final String CONFIG_DIR = "config_dir";
    public static final String SONGS_DIR = "songs_dir";
    public static final String CONFIG_FILE = "config_file";

    // --- Actions ---
    public static final String ACTIONS_FILE = "actions.registry";
    public static final String ACTIONS_DIR = "actions.dir";

    // --- Execution properties ---
    public static final String BELL_ENABLE = "bell.enable";
    public static final String BELL_SOUND_FILE = "bell.sound_file";

    public AppConfig(View view) {
        this.view = view;
        this.props = new Properties(getDefaultProperties());
        this.actions = new Properties();
    }

    public View getView() {
        return view;
    }

    public static Properties getDefaultProperties() {
        Properties defaultProperties = new Properties();

        // --- Storage ---
        defaultProperties.setProperty(CONFIG_DIR, System.getProperty("user.home") + File.separatorChar + "SwitchSoundServer");
        defaultProperties.setProperty(SONGS_DIR, defaultProperties.getProperty(CONFIG_DIR) + File.separatorChar + "sounds");
        defaultProperties.setProperty(ACTIONS_DIR, defaultProperties.getProperty(CONFIG_DIR) + File.separatorChar + "actions");

        // -- Specific files --
        defaultProperties.setProperty(CONFIG_FILE, defaultProperties.getProperty(CONFIG_DIR) + File.separatorChar + "config.props");
        defaultProperties.setProperty(ACTIONS_FILE, defaultProperties.getProperty(ACTIONS_DIR) + File.separatorChar + "actions.props");

        // --- Execution properties ---
        defaultProperties.setProperty(BELL_ENABLE, "false");
        defaultProperties.setProperty(BELL_SOUND_FILE, defaultProperties.getProperty(SONGS_DIR) + File.separatorChar + "DZWONEK.wav");

        return defaultProperties;
    }

    public Properties getProps() {
        return props;
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String getAction(String key) {
        return actions.getProperty(key);
    }

    public Properties getActions() {
        return actions;
    }
}
