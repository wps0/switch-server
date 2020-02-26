package pl.wieczorkep._switch.server.concurrent;

import pl.wieczorkep._switch.server.config.Action;

import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ConcurrencyManager {
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private PriorityQueue<Action> executionQueue;

    public ConcurrencyManager() {
        // Day, hour, minute. SwitchSound.getConfig().getActions().values()
        this.executionQueue = new PriorityQueue<>()
    }
}
