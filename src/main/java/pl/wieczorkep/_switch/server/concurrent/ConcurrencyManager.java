package pl.wieczorkep._switch.server.concurrent;

import static pl.wieczorkep._switch.server.concurrent.ConcurrencyUtils.prettifyThreadName;

public class ConcurrencyManager {
    //    private final ScheduledExecutorService executorService;
//    private PriorityQueue<Action> executionQueue;
//    private Thread nextActionThread;
    private Thread actionExecutorThread;


    public ConcurrencyManager() {
        // Day, hour, minute. SwitchSound.getConfig().getActions().values()
//        this.executionQueue = new PriorityQueue<>()
//        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void init() {
        actionExecutorThread = new Thread(new ActionExecutorThread());
        actionExecutorThread.setName(prettifyThreadName(actionExecutorThread));
        actionExecutorThread.start();


    }

}
