package pl.wieczorkep._switch.server.view;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleView implements View {
    private final Logger consoleLogger;

    public ConsoleView() {
        this.consoleLogger = Logger.getLogger(ConsoleView.class.getName());
        consoleLogger.setLevel(Level.INFO);
    }

    @Override
    public void info(String message) {
        consoleLogger.info(message);
    }

    @Override
    public void error(String message) {
        consoleLogger.severe(message);
    }
}
