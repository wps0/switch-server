package pl.wieczorkep._switch.server.concurrent;

import pl.wieczorkep._switch.server.config.AppConfig;

import static pl.wieczorkep._switch.server.concurrent.ConcurrencyUtils.prettifyThreadName;

public class ConcurrencyManager {
    //    private final ScheduledExecutorService executorService;
//    private PriorityQueue<Action> executionQueue;
//    private Thread nextActionThread;
    private Thread actionExecutorThread;
    private AppConfig appConfig;


    public ConcurrencyManager(AppConfig appConfig) {
        this.appConfig = appConfig;
        // Day, hour, minute. SwitchSound.getConfig().getActions().values()
//        this.executionQueue = new PriorityQueue<>()
//        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void init() {
        appConfig.getView().debug("Creating threads...");
        actionExecutorThread = new Thread(new ActionSupervisorThread(appConfig));
        actionExecutorThread.setName(prettifyThreadName(actionExecutorThread));
        actionExecutorThread.start();

        appConfig.getView().debug("Threads created successfully");
    }

}
