package pl.wieczorkep._switch.server.core.concurrent;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.SoundServer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static pl.wieczorkep._switch.server.core.utils.ConcurrencyUtils.prettifyThreadName;

@Log4j2
public class ConcurrencyManager {
    private Thread actionExecutorThread;
    @Getter
    private SoundServer soundServer;
    @Getter
    private final ReentrantLock lock;
    @Getter
    private final Condition threadErrorCondition;

    public ConcurrencyManager() {
        this.lock = new ReentrantLock();
        this.threadErrorCondition = lock.newCondition();
    }

    public void init(SoundServer srv) {
        LOGGER.debug("Creating threads...");
        soundServer = srv;
        initActionExecutor();
        LOGGER.debug("Threads created successfully");
    }

    public void monitor() {
        LOGGER.info("Monitoring threads...");
        while (true) {
            lock.lock();
            try {
                threadErrorCondition.await();

                if (!actionExecutorThread.isAlive()) {
                    LOGGER.info("ActionExecutorThread is not alive! Initializing...");
                    initActionExecutor();
                }

            } catch (InterruptedException e) {
                LOGGER.info("Monitor thread was interrupted.");
            } finally {
                lock.unlock();
            }
        }
    }

    private void initActionExecutor() {
        LOGGER.debug("Initializing ActionExecutorThread...");
        actionExecutorThread = new Thread(new ActionSupervisorThread(soundServer));
        actionExecutorThread.setName(prettifyThreadName(actionExecutorThread));
        actionExecutorThread.start();
        LOGGER.debug("Initialization successful!");
    }
}
