package pl.wieczorkep._switch.server.concurrent;

import lombok.*;
import pl.wieczorkep._switch.server.config.Action;
import pl.wieczorkep._switch.server.config.AppConfig;

@RequiredArgsConstructor
public class ActionExecutorThread extends Thread {
    private final AppConfig appConfig;
    @Getter
    @NonNull
    private final Action targetAction;
    @Getter
    private boolean successful;
    @Getter
    private boolean finished;

    @Override
    public void run() {
//        int maxDeviation = Integer.parseInt(appConfig.get(ACTION_EXECUTION_TIME_MAX_DEVIATION));
        try {
            appConfig.getView().info("Executing action " + targetAction.getActionId() + " (arguments " + targetAction.getArguments().toString() + ")");

            successful = targetAction.getType().getActionExecutor().execute(targetAction.getArguments());

        } catch (Exception e) {
            appConfig.getView().error("Failed to execute action " + targetAction.getActionId() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
            successful = false;
        } finally {
            appConfig.getView().info("Action " + targetAction.getActionId() + " finished its execution (arguments " + targetAction.getArguments().toString() + ")");
            finished = true;
            appConfig.signalActionChange();
        }
    }
}
