package pl.wieczorkep._switch.server.concurrent;

import pl.wieczorkep._switch.server.SwitchSound;
import pl.wieczorkep._switch.server.config.Action;
import pl.wieczorkep._switch.server.config.AppConfig;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ActionSupervisorThread implements Runnable {
    private boolean running;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledFuture;
    private ActionExecutorThread actionExecutorThread;
    private AppConfig appConfig;
    private ReentrantLock lock;

    public ActionSupervisorThread(AppConfig appConfig) {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.appConfig = appConfig;
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
            appConfig.getView().debug(ConcurrencyUtils.prettifyThreadName(Thread.currentThread()) + " Waiting for the ActionChangeCondition...");

            try {
                appConfig.awaitActionChange();

                scheduleAction();
            } catch (InterruptedException e) {
                appConfig.getView().info("AwaitActionChange condition was interrupted");
            } catch (Exception e) {
                appConfig.getView().error("Exception thrown!");
                e.printStackTrace();

                try {
                    ConcurrencyManager concurrencyManager = SwitchSound.getConcurrencyManager();
                    concurrencyManager.getLock().lock();
                    concurrencyManager.getThreadErrorCondition().signalAll();
                } finally {
                    SwitchSound.getConcurrencyManager().getLock().unlock();
                }
            }
        }
    }

    private void scheduleAction() {
        // moze sie cos popsuc jesli w trakcie, gdy jeden watek sprobuje zaplanowac, drugi doda cos jako firstEntry,
        //  i trzeci sprobuje znowu zaplanowac(?)
        // ToDo: investigate
        Optional<Action> topActionOptional = appConfig.getFirstAction();
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
                appConfig.getView().debug("Remaining time of the current action: " + remainingTime + "; top action's remaining time: " + topAction.getExecutionTime().getTime(MILLISECONDS));

                // Interrupt the current action and plan the new, earlier one.
                if (scheduledFuture.isDone() || remainingTime > topAction.getExecutionTime().getTime(MILLISECONDS)) {
                    // Knowing the new action is more appropriate, cancel the previous one.
                    cancelAction();
                    // Refresh the action's position in the AppConfig internal set.
                    appConfig.refreshPosition(actionExecutorThread.getTargetAction());

                    // The top action is the currently executed one
                    if (remainingTime < 0) {
                        topAction = appConfig.getFirstAction()
                                .orElse(topAction);
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
        appConfig.getView().debug("Cancelling current action...");

        scheduledFuture.cancel(true);
        // todo: czy to nie wywali jakiegos bledu?
        actionExecutorThread.interrupt();

        appConfig.getView().debug("Successfully cancelled current action!");
    }

    private void planAction(Action newAction) {
        appConfig.getView().debug("Planning " + newAction);
        actionExecutorThread = new ActionExecutorThread(appConfig, newAction);
        long s;
        scheduledFuture = executorService.schedule(actionExecutorThread, s = newAction.getExecutionTime().getTime(MILLISECONDS), MILLISECONDS);
        appConfig.getView().debug("Planned (in " + Duration.ofMillis(s).toString() + ") " + newAction + "!");

    }
}
