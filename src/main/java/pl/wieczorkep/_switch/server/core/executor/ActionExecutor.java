package pl.wieczorkep._switch.server.core.executor;

import lombok.NonNull;

import java.util.Properties;

public interface ActionExecutor {
    /**
     * Executes action in a action type-specific manner
     * @param arguments arguments loaded from the action's properties file
     * @return true if action completed successfully, false otherwise
     * @throws InterruptedException when action execution is interrupted
     */
    public boolean execute(Properties arguments) throws InterruptedException;

    /**
     * @return {@link Properties} object containing config values related to (beginning with action-specific path) the
     *  action.
     */
    @NonNull
    public Properties getRelatedProperties();
}
