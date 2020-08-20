package pl.wieczorkep._switch.server.core.concurrent;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.core.Action;
import pl.wieczorkep._switch.server.core.AppConfig;

@Log4j2
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
        try {
            LOGGER.info("Executing action " + targetAction.getActionId() + " (arguments " + targetAction.getArguments().toString() + ")");

            successful = targetAction.getType().getActionExecutor().execute(targetAction.getArguments());

        } catch (InterruptedException e) {
            LOGGER.debug("Action " + targetAction.getActionId() + " was interrupted: " + e.getLocalizedMessage());
            LOGGER.warn(e);
        } catch (Exception e) {
            LOGGER.error("Failed to execute action " + targetAction.getActionId() + ": " + e.getLocalizedMessage());
            LOGGER.error(e);
            successful = false;
        } finally {
            LOGGER.info("Action " + targetAction.getActionId() + " finished its execution (arguments " + targetAction.getArguments().toString() + ")");
            finished = true;
            appConfig.signalActionChange();
        }
    }
}
