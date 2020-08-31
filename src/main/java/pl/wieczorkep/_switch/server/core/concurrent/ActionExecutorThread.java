package pl.wieczorkep._switch.server.core.concurrent;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.SoundServer;
import pl.wieczorkep._switch.server.core.Action;

@Log4j2
public class ActionExecutorThread extends Thread {
    private final SoundServer server;
    @Getter @NonNull
    private Action targetAction;
    @Getter
    private boolean successful;
    @Getter
    private boolean finished;

    public ActionExecutorThread(SoundServer server, @NonNull Action targetAction) {
        this.server = server;
        this.targetAction = targetAction;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Executing action " + targetAction.getActionId() + " (arguments " + targetAction.getArguments().toString() + ")");

            targetAction.getType().getActionExecutor().setAction(targetAction);
            successful = targetAction.getType().getActionExecutor().execute(server);

        } catch (InterruptedException e) {
            LOGGER.info("Action " + targetAction.getActionId() + " was interrupted: " + e.getLocalizedMessage());
            LOGGER.debug(e);
        } catch (Exception e) {
            LOGGER.error("Failed to execute action " + targetAction.getActionId() + ": " + e.getLocalizedMessage());
            LOGGER.error(e);
            successful = false;
        } finally {
            LOGGER.info("Action " + targetAction.getActionId() + " finished its execution (arguments " + targetAction.getArguments().toString() + ")");
            finished = true;
            server.getConfig().signalActionChange();
        }
    }
}
