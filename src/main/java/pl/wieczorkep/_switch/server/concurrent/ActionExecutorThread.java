package pl.wieczorkep._switch.server.concurrent;

import pl.wieczorkep._switch.server.config.Action;
import pl.wieczorkep._switch.server.config.AppConfig;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public class ActionExecutorThread implements Runnable {
    private boolean running;
    private ScheduledExecutorService executorService;
    private Action scheduledAction;
    private AppConfig appConfig;
    //    private Condition actionCondition;
    private ReentrantLock lock;

    public ActionExecutorThread(AppConfig appConfig) {
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
            try {
                appConfig.getView().debug(ConcurrencyUtils.prettifyThreadName(Thread.currentThread()) + " Waiting for the ActionChangeCondition...");

                appConfig.subscribeActionsChangeCondition();

                Map.Entry<String, Action> firstEntry;
                if ((firstEntry = appConfig.getActions().firstEntry()) != null && firstEntry.getValue() != scheduledAction) {
                    Action firstAction = firstEntry.getValue();

                    System.out.printf("Pierwsza akcja: %s; Type: %s(args: %s); ExecutionTime: %s",
                            firstAction.getActionId(), firstAction.getType(), firstAction.getTypeArguments(),
                            firstAction.getExecutionTime().toString());

                    if (scheduledAction == null) {
                        scheduledAction = firstAction;
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                appConfig.getView().debug(Thread.currentThread(), "siema zamykam sie");
            }
        }
    }
}
