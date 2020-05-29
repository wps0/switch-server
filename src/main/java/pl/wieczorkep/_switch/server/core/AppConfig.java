package pl.wieczorkep._switch.server.core;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.wieczorkep._switch.server.view.View;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AppConfig {
    @Getter
    private View view;
    @Getter
    private Properties props;
    // ToDo: problem z watchservice, bo set trzyma akcje w kolejnosci, w jakiej zostaly dodane.
    private TreeSet<Action> actions;
    private static final Logger LOGGER = LogManager.getLogger();

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

    public static final String ACTION_SPOTIFY_APPKEY = "actions.spotify.appkey";
    public static final String ACTION_SPOTIFY_AUTHSCOPES = "actions.spotify.authscope";

    // --- Execution properties ---
    public static final String BELL_ENABLE = "bell.enable";
    public static final String BELL_SOUND_FILE = "bell.sound_file";

    public AppConfig(View view) {
        this.view = view;
        this.actions = new TreeSet<>();
        this.actionsLock = new ReentrantLock();
        this.actionsChangeCondition = this.actionsLock.newCondition();
    }

    /**
     * Should always be called before using other methods
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

    private void put(String key, String value, boolean replace) {
        if (props.get(key) != null && !replace) {
            throw new IllegalStateException("config already contains " + key + " index");
        }
        props.setProperty(key, value);
        try {
            store();
        } catch (IOException e) {
            LOGGER.warn(e);
        }
    }

    public void putIfNotExists(String key, String value) {
        put(key, value, false);
    }

    /**
     * Appends the value to default config file.
     * Warning: if given key already exists, its value is replaced with given one.
     */
    public void put(String key, String value) {
        put(key, value, true);
    }

    // TODO: wziąć to jakoś przepisać
    private void store() throws IOException {
        LOGGER.info("Saving config file...");
        String configFilePath = get(AppConfig.CONFIG_FILE);
        File originalConfig = new File(configFilePath);

        if (!originalConfig.renameTo(new File(configFilePath + ".tmp"))) {
            throw new IOException("cannot rename file " + configFilePath);
        }

        File newConfigFile = new File(configFilePath);
        if (!newConfigFile.createNewFile()) {
            originalConfig.renameTo(new File(configFilePath));
            throw new IOException("cannot create file " + get(AppConfig.CONFIG_FILE));
        }
        FileOutputStream configOutputStream = new FileOutputStream(newConfigFile);
        props.store(configOutputStream, "Config update: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS\n"));
    }

    //
    // === Actions ===
    //
    public Optional<Action> getFirstAction() {
        actionsLock.lock();
        try {
            return Optional.ofNullable(actions.first());
        } catch (NoSuchElementException e) {
            view.debug(e.getMessage() + "; an empty optional will be returned");
            return Optional.empty();
        } finally {
            actionsLock.unlock();
        }
    }

    public void refreshPosition(Action action) {
        actionsLock.lock();
        try {
            List<Action> allActions = new ArrayList<>();

            Iterator<Action> actionIterator = actions.iterator();
            while (actionIterator.hasNext()) {
                Action currentAction = actionIterator.next();
                allActions.add(currentAction);
                actionIterator.remove();
            }

            actions.addAll(allActions);

        } finally {
            actionsLock.unlock();
        }
    }

    public void putAction(Action action) {
        addAction(action);
        view.info("Loaded 1 action.");
    }

    private void addAction(Action action) {
        actionsLock.lock();
        try {
            actions.add(action);

            // signal the action change
            actionsChangeCondition.signalAll();
        } finally {
            actionsLock.unlock();
        }
    }

    public void putActions(Set<? extends Action> actionMap) {
        actionsLock.lock();
        try {
            actions.addAll(actionMap);
            actionsChangeCondition.signalAll();
        } finally {
            actionsLock.unlock();
        }
        view.info("Loaded " + actionMap.size() + " actions");
    }

    //
    // === Actions ===
    //
    public Logger getLogger() {
        return LOGGER;
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

        // --- Spotify properties ---
        defaultProperties.setProperty(ACTION_SPOTIFY_APPKEY, "NjRmNzhjOWE4YTUxNDEzYTg2MzY0ZGQ5OTcwZGFiYjY6MWNmNzFjMTRkYThlNGQ2Y2IyYTRjODM4ODVjZTE0YjQK");
        defaultProperties.setProperty(ACTION_SPOTIFY_AUTHSCOPES, "user-read-playback-state,user-modify-playback-state," +
                        "playlist-read-collaborative,user-read-playback-position,user-read-currently-playing," +
                        "playlist-read-private,app-remote-control");
        return defaultProperties;
    }

    public static long getReferenceTime() {
        return Instant.now().getEpochSecond();
    }
}
