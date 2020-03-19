package pl.wieczorkep._switch.server.concurrent;

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
//        this.actionCondition = this.lock.newCondition();
    }

    @Override
    public void run() {
//        Action firstAction = SwitchSound.getConfig().getActions().firstEntry().getValue();
        this.running = true;
        loop();
    }

    public void loop() {

        while (running) {
            appConfig.getView().debug(ConcurrencyUtils.prettifyThreadName(Thread.currentThread()) + " Waiting for the ActionChangeCondition...");

            try {
//                if (actionExecutorThread == null && appConfig.getActionsFirstEntry() != null) {
//                    Action potentialAction;
////                    appConfig.getView().info("Planning action " + );
//                    actionExecutorThread = new ActionExecutorThread(appConfig, appConfig.getActionsFirstEntry().getValue());
//                    scheduledAction = executorService.schedule(actionExecutorThread, 3, TimeUnit.SECONDS);
//                }
//
//                if (actionExecutorThread != null) {
//                    if (!actionExecutorThread.isAlive()) {
//                        scheduleAction();
//                    }
//                }

                scheduleAction();

                appConfig.awaitActionChange();


//                Map.Entry<String, Action> firstEntry = appConfig.getActions().firstEntry();
//                if (firstEntry != null && firstEntry.getValue() != scheduledAction) {
//                    Action firstAction = firstEntry.getValue();
//
//                    System.out.printf("Pierwsza akcja: %s; Type: %s(args: %s); ExecutionTime: %s",
//                            firstAction.getActionId(), firstAction.getType(), firstAction.getRawArguments(),
//                            firstAction.getExecutionTime().toString());
//
//                    if (scheduledAction == null) {
//                        scheduledAction = firstAction;
//                    }
//                }

            } catch (InterruptedException e) {
                e.printStackTrace();
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

        // todo:
        //  1. czy sie oplaca zaplanowac?
        //  2. cancel poprzedniej akcji
        //  3. zaplanuj

        // The currently planned action is waiting to be executed,
        //  so check whether there is a new top action.
        lock.lock();
        try {
            if (scheduledFuture != null) {
                long remainingTime = scheduledFuture.getDelay(MILLISECONDS);
                appConfig.getView().debug("Remaining time of the current action: " + remainingTime + "; top action's remaining time: " + topAction.getExecutionTime().getTime(MILLISECONDS));

                // Interrupt the current action and plan the new, earlier one.
                if (scheduledFuture.isDone() || remainingTime > topAction.getExecutionTime().getTime(MILLISECONDS)) {

                    cancelAction();
                    // Refresh the action's position in the AppConfig internal set.
                    appConfig.refreshPosition(actionExecutorThread.getTargetAction());

                    // The top action is the currently executed one
                    System.out.println("czas pozostaly: " + remainingTime);
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

//        actionExecutorThread = new ActionExecutorThread(appConfig, appConfig.getActionsFirstEntry().getValue());
//        scheduledAction = executorService.schedule(actionExecutorThread, 3, TimeUnit.SECONDS);
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
