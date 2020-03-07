package pl.wieczorkep._switch.server.concurrent;

import pl.wieczorkep._switch.server.config.AppConfig;

import java.util.concurrent.*;

public class ActionSupervisorThread implements Runnable {
    private boolean running;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledAction;
    private ActionExecutorThread actionExecutorThread;
    private AppConfig appConfig;

    public ActionSupervisorThread(AppConfig appConfig) {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.appConfig = appConfig;
        this.running = false;
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
                if (actionExecutorThread == null && appConfig.getActionsFirstEntry() != null) {
                    System.out.println("siema planuje");
                    actionExecutorThread = new ActionExecutorThread(appConfig, appConfig.getActionsFirstEntry().getValue());
                    scheduledAction = executorService.schedule(actionExecutorThread, 5, TimeUnit.SECONDS);
                }

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
            } finally {
                appConfig.getView().debug(Thread.currentThread(), "siema zamykam sie");
            }
        }
    }
}
