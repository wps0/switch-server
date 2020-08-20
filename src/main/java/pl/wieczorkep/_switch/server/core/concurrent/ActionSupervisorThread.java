package pl.wieczorkep._switch.server.core.concurrent;

import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.Server;
import pl.wieczorkep._switch.server.core.Action;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Log4j2
public class ActionSupervisorThread implements Runnable {
    private boolean running;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledFuture;
    private ActionExecutorThread actionExecutorThread;
    private Server server;
    private ReentrantLock lock;

    public ActionSupervisorThread(Server server) {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.server = server;
        this.running = false;
        this.lock = new ReentrantLock();
    }

    @Override
    public void run() {
        this.running = true;
        loop();
    }

    public void loop() {
        scheduleAction();

        while (running) {
            LOGGER.debug(ConcurrencyUtils.prettifyThreadName(Thread.currentThread()) + " Waiting for the ActionChangeCondition...");

            try {
                server.getConfig().awaitActionChange();

                scheduleAction();
            } catch (InterruptedException e) {
                LOGGER.info("AwaitActionChange condition was interrupted");
            } catch (Exception e) {
                LOGGER.error("Exception thrown!");
                e.printStackTrace();

                try {
                    ConcurrencyManager concurrencyManager = server.getConcurrencyManager();
                    concurrencyManager.getLock().lock();
                    concurrencyManager.getThreadErrorCondition().signalAll();
                } finally {
                    server.getConcurrencyManager().getLock().unlock();
                }
            }
        }
    }

    private void scheduleAction() {
        // moze sie cos popsuc jesli w trakcie, gdy jeden watek sprobuje zaplanowac, drugi doda cos jako firstEntry,
        //  i trzeci sprobuje znowu zaplanowac(?)
        // ToDo: investigate
        Optional<Action> topActionOptional = server.getConfig().getFirstAction();
        if (!topActionOptional.isPresent()) {
            return;
        }

        Action topAction = topActionOptional.get();

        // The currently planned action is waiting to be executed,
        //  so check whether there is a new top action.
        lock.lock();
        try {
            if (scheduledFuture != null) {
                long remainingTime = scheduledFuture.getDelay(MILLISECONDS);
                LOGGER.debug("Remaining time of the current action: " + remainingTime + "; top action's remaining time: " + topAction.getExecutionTime().getTime(MILLISECONDS));

                // Interrupt the current action and plan the new, earlier one.
                if (scheduledFuture.isDone() || remainingTime > topAction.getExecutionTime().getTime(MILLISECONDS)) {
                    // Knowing the new action is more appropriate, cancel the previous one.
                    cancelAction();
                    // Refresh the action's position in the AppConfig internal set.
                    server.getConfig().refreshPosition(actionExecutorThread.getTargetAction());

                    // The top action is the currently executed one
                    if (remainingTime < 0) {
                        topAction = server.getConfig().getFirstAction().orElse(topAction);
                    }

                    planAction(topAction);
                }

            } else {
                // First run
                planAction(topAction);
            }
        } finally {
            lock.unlock();
        }
    }

    private void cancelAction() {
        LOGGER.debug("Cancelling current action...");

        scheduledFuture.cancel(true);
        // todo: czy to nie wywali jakiegos bledu?
        actionExecutorThread.interrupt();

        LOGGER.debug("Successfully cancelled current action!");
    }

    private void planAction(Action newAction) {
        LOGGER.debug("Planning " + newAction);
        actionExecutorThread = new ActionExecutorThread(server.getConfig(), newAction);
        long s;
        scheduledFuture = executorService.schedule(actionExecutorThread, s = newAction.getExecutionTime().getTime(MILLISECONDS), MILLISECONDS);
        LOGGER.debug("Planned (in " + Duration.ofMillis(s).toString() + ") " + newAction + "!");

    }
}
