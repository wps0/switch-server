package pl.wieczorkep._switch.server.config.executor;

import pl.wieczorkep._switch.server.controller.SoundController;

import java.util.Properties;

public class SoundExecutor implements ActionExecutor {
    @Override
    public boolean execute(Properties arguments) {
        // ToDo: ... execute sound action
        SoundController.main(null);
        return false;
    }
}
