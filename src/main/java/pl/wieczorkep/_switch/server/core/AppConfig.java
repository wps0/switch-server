package pl.wieczorkep._switch.server.core;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static pl.wieczorkep._switch.server.Constants.CONFIG_FILE;
import static pl.wieczorkep._switch.server.core.utils.ConfigUtils.getDefaultConfig;
import static pl.wieczorkep._switch.server.core.utils.FileSystemUtils.setFilePermissions;

@Log4j2
public class AppConfig {
    @Getter
    private Properties props;
    // ToDo: problem z watchservice, bo set trzyma akcje w kolejnosci, w jakiej zostaly dodane.
    private TreeSet<Action> actions;

    // ==!== Concurrency support ==!==
    @Getter
    private ReentrantLock actionsLock;
    private Condition actionsChangeCondition;


    public AppConfig() {
        this.actions = new TreeSet<>();
        this.actionsLock = new ReentrantLock();
        this.actionsChangeCondition = this.actionsLock.newCondition();
    }

    /**
     * Should always be called before using other methods
     */
    public void init() {
        this.props = new Properties(getDefaultConfig());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Storage operations
    ///////////////////////////////////////////////////////////////////////////
    @Nullable
    public String get(@NotNull String key) {
        return props.getProperty(key);
    }

    public String getOrDefault(@NotNull String key, String def) {
        return props.getProperty(key, def);
    }

    /**
     * Finds all properties that match given prefix path. For example:
     * <pre>{@code getPropertiesInPath("config.fs");}</pre> will return "config.fs.dir", but not "config.fss.dir".
     * Right now multiple matching (eg. "config.*.dir") is not supported.
     * @param path Path of keys in config.
     * @return Properties matching given path.
     */
    @NonNull
    public Properties getPropertiesInPath(@NonNull String path) {
        Enumeration<?> propNames = props.propertyNames();
        Properties result = new Properties();
        while (propNames.hasMoreElements()) {
            String key = (String) propNames.nextElement();
            if (key.length() >= path.length() && key.startsWith(path)) {
                result.put(key, props.get(key));
            }
        }
        return result;
    }

    private void put(@NonNull String key, String value, boolean replace) {
        if (!replace && props.get(key) != null) {
            throw new IllegalStateException("config already contains key " + key);
        }
        // Don't waste time for saving identical values
        if (props.getProperty(key).equals(value)) {
            return;
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
     * Appends the value to config file.
     * Warning: if given key already exists, its value will be replaced with the new one.
     */
    public <T> void put(String key, T value) {
        put(key, value.toString(), true);
    }

    private void store() throws IOException {
        LOGGER.info("Saving config file...");
        String configFilePath = get(CONFIG_FILE);
        File originalConfig = new File(configFilePath);

        if (!originalConfig.renameTo(new File(configFilePath + ".tmp"))) {
            throw new IOException("cannot rename file " + configFilePath);
        }

        File newConfigFile = new File(configFilePath);
        // revert old config if can't create the new one
        if (!newConfigFile.createNewFile()) {
            // TODO: to miejsce trzeba mieć na uwadze, bo może się zdesyncować config na dysku z configiem w ramie
            originalConfig.renameTo(new File(configFilePath));
            throw new IOException("cannot create file " + get(CONFIG_FILE));
        }
        // sets rw permissions only for file owner
        setFilePermissions(newConfigFile);
        // store updated config
        @Cleanup FileOutputStream configOutputStream = new FileOutputStream(newConfigFile);
        props.store(configOutputStream, "Auto saved config");
        // delete tmp (old) config
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
            LOGGER.trace(e.getMessage() + "; an empty optional will be returned");
            return Optional.empty();
        } finally {
            actionsLock.unlock();
        }
    }

    // TODO: zoptymalizować
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

    public boolean putAction(Action action) {
        actionsLock.lock();
        boolean status = false;
        try {
            status = actions.add(action);
            // signal action change
            actionsChangeCondition.signalAll();
        } finally {
            actionsLock.unlock();

            if (status) {
                LOGGER.info("Loaded 1 action");
            } else {
                LOGGER.error("Failed to load 1 action!");
            }
        }
        return status;
    }

    public boolean putActions(Set<? extends Action> actionMap) {
        actionsLock.lock();
        boolean status = false;
        try {
            status = actions.addAll(actionMap);
            // signal action change
            actionsChangeCondition.signalAll();
        } finally {
            actionsLock.unlock();
            if (status) {
                LOGGER.info("Loaded " + actionMap.size() + " actions");
            } else {
                LOGGER.error("Failed to load " + actionMap.size() + " actions!");
            }
        }

        return status;
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
}