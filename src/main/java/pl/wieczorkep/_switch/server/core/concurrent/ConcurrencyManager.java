package pl.wieczorkep._switch.server.core.concurrent;

import lombok.Getter;
import pl.wieczorkep._switch.server.core.AppConfig;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static pl.wieczorkep._switch.server.core.concurrent.ConcurrencyUtils.prettifyThreadName;

public class ConcurrencyManager {
    private Thread actionExecutorThread;
    private final AppConfig appConfig;
    @Getter
    private final ReentrantLock lock;
    @Getter
    private final Condition threadErrorCondition;

    public ConcurrencyManager(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.lock = new ReentrantLock();
        this.threadErrorCondition = lock.newCondition();
    }

    public void init() {
        appConfig.getView().debug("Creating threads...");
        initActionExecutor();

        appConfig.getView().debug("Threads created successfully");
    }

    public void monitor() {
        appConfig.getView().info("Monitoring threads...");
        while (true) {
            lock.lock();
            try {
                threadErrorCondition.await();

                if (!actionExecutorThread.isAlive()) {
                    appConfig.getView().info("ActionExecutorThread is not alive! Initializing...");
                    initActionExecutor();
                }

            } catch (InterruptedException e) {
                appConfig.getView().info("Monitor thread was interrupted.");
            } finally {
                lock.unlock();
            }
        }
    }

    private void initActionExecutor() {
        appConfig.getView().debug("Initializing ActionExecutorThread...");
        actionExecutorThread = new Thread(new ActionSupervisorThread(appConfig));
        actionExecutorThread.setName(prettifyThreadName(actionExecutorThread));
        actionExecutorThread.start();
        appConfig.getView().debug("Initialization successful!");
    }
}
