package pl.wieczorkep._switch.server.config;

import lombok.Getter;
import pl.wieczorkep._switch.server.view.View;

import java.io.File;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AppConfig {
    @Getter
    private View view;
    @Getter
    private Properties props;
    private TreeMap<String, Action> actions;
    private Action firstAction;
    // ==!== Concurrency support ==!==
    @Getter
    private ReentrantLock actionsLock;
    private Condition actionsChangeCondition;

    // ==!== Config variables ==!==
    public static final String CONFIG_DIR = "config_dir";
    public static final String SONGS_DIR = "songs_dir";
    public static final String CONFIG_FILE = "config_file";

    // --- Actions ---
    public static final String ACTIONS_FILE = "actions.registry";
    public static final String ACTIONS_DIR = "actions.dir";

    /**
     * Max deviation of the specified execution time in seconds
     */
    public static final String ACTION_EXECUTION_TIME_MAX_DEVIATION = "acitons.maxDeviation";

    // --- Execution properties ---
    public static final String BELL_ENABLE = "bell.enable";
    public static final String BELL_SOUND_FILE = "bell.sound_file";

    public AppConfig(View view) {
        this.view = view;
        this.actions = new TreeMap<>();
        this.actionsLock = new ReentrantLock();
        this.actionsChangeCondition = this.actionsLock.newCondition();
        this.firstAction = null;
    }


    /**
     * Should always be called before using the other methods
     */
    public void init() {
        this.props = new Properties(getDefaultProperties());
        view.init();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Storage operations
    ///////////////////////////////////////////////////////////////////////////
    public String get(String key) {
        return props.getProperty(key);
    }

    //
    // === Actions ===
    //
    public Action getAction(String key) {
        actionsLock.lock();
        try {
            return actions.get(key);
        } finally {
            actionsLock.unlock();
        }
    }

    public Optional<Action> getFirstAction() {
        actionsLock.lock();
        try {
//            return actions.firstEntry();
            if (firstAction == null) {
                return Optional.empty();
            }
            return Optional.of(firstAction);
        } finally {
            actionsLock.unlock();
        }
    }

    public void putAction(Action action) {
        putAction(action.getActionId(), action);
        view.info("Loaded 1 action.");
    }

    private void putAction(String actionId, Action action) {
        actionsLock.lock();
        try {
            actions.put(actionId, action);

            if (firstAction == null || firstAction.compareTo(action) > 0) {
                firstAction = action;
            }

            // signal the action change
            actionsChangeCondition.signalAll();
        } finally {
            actionsLock.unlock();
        }
    }

    public void putActions(Map<? extends String, ? extends Action> actionMap) {
        actionMap.forEach(this::putAction);
        actionsLock.lock();
        try {
            actionsChangeCondition.signalAll();
        } finally {
            actionsLock.unlock();
        }
        view.info("Loaded " + actionMap.size() + " actions");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Multithreading
    ///////////////////////////////////////////////////////////////////////////
    public void awaitActionChange() throws InterruptedException {
        actionsLock.lock();
        try {
            actionsChangeCondition.await();
        } finally {
            actionsLock.unlock();
        }
    }

    public void signalActionChange() {
        actionsLock.lock();
        try {
            actionsChangeCondition.signalAll();
        } finally {
            actionsLock.unlock();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static methods
    ///////////////////////////////////////////////////////////////////////////
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
        defaultProperties.setProperty(ACTION_EXECUTION_TIME_MAX_DEVIATION, "30");

        // --- Execution properties ---
        defaultProperties.setProperty(BELL_ENABLE, "false");
        defaultProperties.setProperty(BELL_SOUND_FILE, defaultProperties.getProperty(SONGS_DIR) + File.separatorChar + "DZWONEK.wav");

        return defaultProperties;
    }
}
