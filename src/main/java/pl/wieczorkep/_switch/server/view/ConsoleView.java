package pl.wieczorkep._switch.server.view;

import lombok.Getter;
import pl.wieczorkep._switch.server.core.concurrent.ConcurrencyUtils;
import pl.wieczorkep._switch.server.view.menu.Menu;

import java.util.Scanner;
import java.util.logging.*;

public class ConsoleView implements View {
    private final Logger consoleLogger;
    private Scanner scanner;
    @Getter
    private Menu menu;

    public ConsoleView() {
        this.scanner = new Scanner(System.in);
        this.menu = new Menu(this);
        this.consoleLogger = Logger.getLogger(ConsoleView.class.getName());
    }

    @Override
    public void init() {
        // == logger init ==
        consoleLogger.setUseParentHandlers(false);

        Handler consoleHandler = new ConsoleHandler();
        Formatter formatter = new LogFormatter();
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(Level.ALL);
        consoleLogger.addHandler(consoleHandler);


        consoleLogger.setLevel(Level.ALL);
        consoleLogger.info("Logging level is: " + consoleLogger.getLevel());
    }

    @Override
    public <T> void info(T message) {
        String msg;
        if (message instanceof String) {
            msg = (String) message;
        } else {
            msg = String.valueOf(message);
        }
        consoleLogger.info(msg);
    }

    @Override
    public void error(String message) {
        consoleLogger.severe(message);
    }

    @Override
    public void config(String message) {
        consoleLogger.finer(message);
    }

    @Override
    public <T> void debug(T message) {
        String msg;
        if (message instanceof String) {
            msg = (String) message;
        } else {
            msg = String.valueOf(message);
        }
        consoleLogger.fine(msg);
    }

    @Override
    public void debug(Thread thread, String message) {
        debug(ConcurrencyUtils.prettifyThreadName(Thread.currentThread()) + " " + message);
    }

    @Override
    public int readInt(String label) {
        System.out.format("%s: ", label);
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

}
