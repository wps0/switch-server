package pl.wieczorkep._switch.server.core;

import lombok.Cleanup;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.wieczorkep._switch.server.view.View;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
    // --- Prefixes ---
    protected static final String PREFIX_ACTION = "actions.";
    protected static final String PREFIX_SPOTIFY = PREFIX_ACTION + "spotify.";
    protected static final String PREFIX_FILESYSTEM = "fs.";

    // --- Filesystem ---
    public static final String CONFIG_DIR = PREFIX_FILESYSTEM + "config_dir";
    public static final String SONGS_DIR = PREFIX_FILESYSTEM + "songs_dir";
    public static final String CONFIG_FILE = PREFIX_FILESYSTEM + "config_file";

    // --- Actions ---
    public static final String ACTIONS_FILE = PREFIX_ACTION + "registry";
    public static final String ACTIONS_DIR = PREFIX_ACTION + "dir";

    public static final String ACTION_SPOTIFY_APPID = PREFIX_SPOTIFY + "app-id";
    public static final String ACTION_SPOTIFY_APPSECRET = PREFIX_SPOTIFY + "app-secret";
    public static final String ACTION_SPOTIFY_AUTHSCOPES = PREFIX_SPOTIFY + "auth-scope";
    public static final String ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH = PREFIX_SPOTIFY + "client.refresh-token";
    public static final String ACTION_SPOTIFY_CLIENT_TOKEN = PREFIX_SPOTIFY + "client.token";
    public static final String ACTION_SPOTIFY_CLIENT_TOKEN_VALIDITY = PREFIX_SPOTIFY + "client.token.validity";
    public static final String ACTION_SPOTIFY_CLIENT_TMPCODE = PREFIX_SPOTIFY + "client.tmp-code";
    public static final String ACTION_SPOTIFY_CLIENT_DEFAULTDEVICE = PREFIX_SPOTIFY + "client.default-device";

    public static final String BELL_ENABLE = PREFIX_ACTION + "bell.enable";
    public static final String BELL_SOUND_FILE = PREFIX_ACTION + "bell.sound_file";

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
    @Nullable
    public String get(@NotNull String key) {
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
        @Cleanup
        FileOutputStream configOutputStream = new FileOutputStream(newConfigFile);
        props.store(configOutputStream, "Auto saved config");

        // TODO: SECURITY: jakaś lepsza serializacja configu. Można w tym miejscu usunąć dowolny plik kończący się na
        //  .tmp z permisjami usera wykonującego aplikajcę (???)
        Files.delete(Path.of(new File(configFilePath + ".tmp").toURI()));
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
        defaultProperties.setProperty(ACTION_SPOTIFY_APPID, "64f78c9a8a51413a86364dd9970dabb6");
        defaultProperties.setProperty(ACTION_SPOTIFY_APPSECRET, "");
        defaultProperties.setProperty(ACTION_SPOTIFY_AUTHSCOPES, "user-read-playback-state,user-modify-playback-state," +
                "playlist-read-collaborative,user-read-playback-position,user-read-currently-playing," +
                "playlist-read-private,app-remote-control");
        defaultProperties.setProperty(ACTION_SPOTIFY_CLIENT_TOKEN, "");
        defaultProperties.setProperty(ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH, "");
        defaultProperties.setProperty(ACTION_SPOTIFY_CLIENT_TMPCODE, "");
        defaultProperties.setProperty(ACTION_SPOTIFY_CLIENT_DEFAULTDEVICE, "");
        return defaultProperties;
    }

    public static long getReferenceTime() {
        return Instant.now().getEpochSecond();
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Logger getLogger(Class<?> callingClass) {
        return LogManager.getLogger(callingClass);
    }
}
