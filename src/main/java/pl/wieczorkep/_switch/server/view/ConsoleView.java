package pl.wieczorkep._switch.server.view;

import lombok.Getter;
import pl.wieczorkep._switch.server.menu.Menu;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleView implements View {
    private final Logger consoleLogger;
    private Scanner scanner;
    private @Getter Menu menu;

    public ConsoleView() {
        this.consoleLogger = Logger.getLogger(ConsoleView.class.getName());
        consoleLogger.setLevel(Level.INFO);
        scanner = new Scanner(System.in);
        menu = new Menu(this);
    }

    @Override
    public <T> void info(T message) {
        String msg;
        if (message instanceof String)
            msg = (String) message;
        else
            msg = String.valueOf(message);
        consoleLogger.info(msg);
    }

    @Override
    public void error(String message) {
        consoleLogger.severe(message);
    }

    @Override
    public int readInt(String label) {
        System.out.format("%s: ", label);
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }
}
