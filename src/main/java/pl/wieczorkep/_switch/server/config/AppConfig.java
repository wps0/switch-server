package pl.wieczorkep._switch.server.config;

import pl.wieczorkep._switch.server.view.View;

import java.io.File;
import java.util.Properties;

public class AppConfig {
    private View view;
    private Properties props;

    public static final String CONFIG_DIR = "config_dir";
    public static final String SONGS_DIR = "songs_dir";
    public static final String CONFIG_FILE = "config_file";
    public static final String BELL_SOUND_FILE = "bell_sound_file";

    public AppConfig(View view) {
        this.view = view;
        this.props = new Properties(getDefaultProperties());
    }

    public View getView() {
        return view;
    }

    public static Properties getDefaultProperties() {
        Properties defaultProperties = new Properties();

        // --- Storage ---
        defaultProperties.setProperty(CONFIG_DIR, System.getProperty("user.home") + File.separatorChar + "SwitchSoundServer");
        defaultProperties.setProperty(SONGS_DIR, defaultProperties.getProperty(CONFIG_DIR) + File.separatorChar + "sounds");
        // -- Specific files --
        defaultProperties.setProperty(CONFIG_FILE, defaultProperties.getProperty(CONFIG_DIR) + File.separatorChar + "config.props");
        defaultProperties.setProperty(BELL_SOUND_FILE, defaultProperties.getProperty(SONGS_DIR) + File.separatorChar + "DZWONEK.wav");

        return defaultProperties;
    }

    public Properties getProps() {
        return props;
    }

    public String get(String key) {
        return props.getProperty(key);
    }
}
