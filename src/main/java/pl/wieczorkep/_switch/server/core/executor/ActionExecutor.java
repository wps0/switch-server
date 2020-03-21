package pl.wieczorkep._switch.server.core.executor;

import java.util.Properties;

public interface ActionExecutor {
    public boolean execute(Properties arguments) throws InterruptedException;
}
