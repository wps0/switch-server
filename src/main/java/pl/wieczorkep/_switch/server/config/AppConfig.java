package pl.wieczorkep._switch.server.config;

import lombok.Getter;
import pl.wieczorkep._switch.server.view.View;

import java.io.File;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class AppConfig {
    @Getter
    private View view;
    @Getter
    private Properties props;
    @Getter
    private TreeMap<String, Action> actions;
    @Getter
    private ReentrantLock lock;

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
        this.actions = new TreeMap<>();
        this.lock = new ReentrantLock();
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public Action getAction(String key) {
        lock.lock();
        try {
            return actions.get(key);
        } finally {
            lock.unlock();
        }
    }

    public void putActions(Map<? extends String, ? extends Action> actionMap) {
        lock.lock();
        try {
            actions.putAll(actionMap);
            view.info("Loaded " + actionMap.size() + " actions");
        } finally {
            lock.unlock();
        }
    }
//
//    public void subscribeForNewActions() throws InterruptedException {
//
//    }
//
//    public void siema() {
//        System.out.println("OKS");
//    }

    ///////////////////////////////////////////////////////////////////////////
    // Static methods
    ///////////////////////////////////////////////////////////////////////////
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
}
