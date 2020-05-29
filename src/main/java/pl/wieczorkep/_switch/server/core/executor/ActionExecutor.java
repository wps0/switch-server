package pl.wieczorkep._switch.server.core.executor;

import java.util.Properties;

public interface ActionExecutor {
    /**
     * Executes action in a action type-specific manner
     * @param arguments arguments loaded from the action's properties file
     * @return true if action completed successfully, false otherwise
     * @throws InterruptedException when action execution is interrupted
     */
    public boolean execute(Properties arguments) throws InterruptedException;
}
