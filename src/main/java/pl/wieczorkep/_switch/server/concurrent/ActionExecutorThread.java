package pl.wieczorkep._switch.server.concurrent;

import lombok.Getter;
import lombok.NonNull;
import pl.wieczorkep._switch.server.config.Action;
import pl.wieczorkep._switch.server.config.AppConfig;

public class ActionExecutorThread extends Thread {
    private final AppConfig appConfig;
    @Getter
    @NonNull
    private Action targetAction;
    @Getter
    private boolean successful;
    @Getter
    private boolean finished;

    public ActionExecutorThread(AppConfig appConfig, @NonNull Action targetAction) {
        this.appConfig = appConfig;
        this.targetAction = targetAction;
    }

    @Override
    public void run() {
//        int maxDeviation = Integer.parseInt(appConfig.get(ACTION_EXECUTION_TIME_MAX_DEVIATION));
        try {
            appConfig.getView().info("Executing action " + targetAction.getActionId() + " (arguments " + targetAction.getArguments().toString() + ")");

            successful = targetAction.getType().getActionExecutor().execute(targetAction.getArguments());

        } catch (InterruptedException e) {
            appConfig.getView().debug("Action " + targetAction.getActionId() + " was interrupted: " + e.getLocalizedMessage());
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
