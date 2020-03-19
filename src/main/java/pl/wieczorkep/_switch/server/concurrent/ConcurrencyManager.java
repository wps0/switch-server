package pl.wieczorkep._switch.server.concurrent;

import pl.wieczorkep._switch.server.config.AppConfig;

import static pl.wieczorkep._switch.server.concurrent.ConcurrencyUtils.prettifyThreadName;

public class ConcurrencyManager {
    private Thread actionExecutorThread;
    private AppConfig appConfig;


    public ConcurrencyManager(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void init() {
        // ToDo: add thread monitoring feature (and the ability to recover from errors)
        appConfig.getView().debug("Creating threads...");
        actionExecutorThread = new Thread(new ActionSupervisorThread(appConfig));
        actionExecutorThread.setName(prettifyThreadName(actionExecutorThread));
        actionExecutorThread.start();

        appConfig.getView().debug("Threads created successfully");
    }

}
