package pl.wieczorkep._switch.server.core.executor;

import pl.wieczorkep._switch.server.SoundServer;
import pl.wieczorkep._switch.server.core.Action;

public interface IActionExecutor {
    /**
     * Executes action in a action type-specific manner
     * @param server Server to which the executor belongs.
     * @return true if action completed successfully, false otherwise
     * @throws InterruptedException when action execution is interrupted
     */
    boolean execute(SoundServer server) throws InterruptedException;

    /**
     * @param action Action instance which is being executed by this executor.
     */
    void setAction(Action action);
}
