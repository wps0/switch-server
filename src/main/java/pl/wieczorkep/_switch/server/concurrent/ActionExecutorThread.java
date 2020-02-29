package pl.wieczorkep._switch.server.concurrent;

import pl.wieczorkep._switch.server.SwitchSound;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ActionExecutorThread implements Runnable {
    private ScheduledExecutorService executorService;
    private boolean running;

    public ActionExecutorThread() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.running = false;
    }

    @Override
    public void run() {
//        Action firstAction = SwitchSound.getConfig().getActions().firstEntry().getValue();
        this.running = true;
        loop();

//        System.out.printf("Pierwsza akcja: %s; Type: %s(args: %s); ExecutionTime: %s",
//                firstAction.getActionId(), firstAction.getType(), firstAction.getTypeArguments(),
//                firstAction.getExecutionTime().toString());
    }

    public synchronized void loop() {

        while (running) {
            int previousSize = SwitchSound.getConfig().getActions().size();
            try {
                while (previousSize == SwitchSound.getConfig().getActions().size()) {
                    SwitchSound.getConfig().getLock().w;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                running = false;
            }
            System.out.println("CZEKAM CIAGLE:)");
        }
    }
}
