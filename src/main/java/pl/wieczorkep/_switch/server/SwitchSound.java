package pl.wieczorkep._switch.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.core.concurrent.ConcurrencyManager;

@Log4j2
@RequiredArgsConstructor
public class SwitchSound {
    @Getter
    private final AppConfig config;
    @Getter
    private final ConcurrencyManager cm;

    // TODO: arguments handling
    public static void main(String[] args) {
        // set logger level to Level.ALL for debug purposes
        Configurator.setRootLevel(Level.ALL);
        // prepare vars
        AppConfig config = new AppConfig();
        ConcurrencyManager cm = new ConcurrencyManager();
        SwitchSound switchSound = new SwitchSound(config, cm);
        // run the app
        switchSound.run();
    }

    public void run() {
        // init server
        Server serverInstance = new Server(config, cm);
        LOGGER.info("Initializing server...");
        serverInstance.init();
        // run server
        LOGGER.info("Running the server...");
        serverInstance.run();
        LOGGER.info("The server finished execution.");
    }
}
