package pl.wieczorkep._switch.server.concurrent;

import lombok.RequiredArgsConstructor;
import pl.wieczorkep._switch.server.config.Action;
import pl.wieczorkep._switch.server.config.AppConfig;

@RequiredArgsConstructor
public class ActionExecutorThread implements Runnable {
    private final AppConfig appConfig;
    private final Action targetAction;

    @Override
    public void run() {
//        int maxDeviation = Integer.parseInt(appConfig.get(ACTION_EXECUTION_TIME_MAX_DEVIATION));


    }
}
