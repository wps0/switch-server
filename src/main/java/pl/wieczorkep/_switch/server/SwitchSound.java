package pl.wieczorkep._switch.server;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.core.concurrent.ConcurrencyManager;

@Log4j2
@RequiredArgsConstructor
public class SwitchSound {
    @Getter @NonNull
    private final SoundServer soundServerInstance;

    public static void main(String[] args) {
        // set logger level to Level.ALL for debug purposes
        Configurator.setRootLevel(Level.ALL);
        // prepare vars
        AppConfig cfg = new AppConfig();
        ConcurrencyManager cm = new ConcurrencyManager();
        SwitchSound switchSound = new SwitchSound(new SoundServer(cfg, cm));
        // run the app
        switchSound.run();
    }

    public void run() {
        // init server
        LOGGER.info("Initializing server...");
        soundServerInstance.init();
        // run server
        LOGGER.info("Running the server...");
        soundServerInstance.run();
        LOGGER.info("Server started.");
    }
}
