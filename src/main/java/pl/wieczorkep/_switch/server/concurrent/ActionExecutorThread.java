package pl.wieczorkep._switch.server.concurrent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.wieczorkep._switch.server.config.Action;
import pl.wieczorkep._switch.server.config.AppConfig;

@RequiredArgsConstructor
public class ActionExecutorThread implements Runnable {
    private final AppConfig appConfig;
    private final Action targetAction;
    @Getter
    private boolean status;
    @Getter
    private boolean finished;

    @Override
    public void run() {
//        int maxDeviation = Integer.parseInt(appConfig.get(ACTION_EXECUTION_TIME_MAX_DEVIATION));
        try {
            appConfig.getView().info("Executing action " + targetAction.getActionId() + " with arguments " + targetAction.getArguments().toString());

            status = targetAction.getType().getActionExecutor().execute(targetAction.getArguments());
            
        } catch (Exception e) {
            appConfig.getView().error("Failed to execute action " + targetAction.getActionId() + "");
            status = false;
        } finally {
            finished = true;
            appConfig.signalActionChange();
        }
    }
}
