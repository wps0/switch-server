package pl.wieczorkep._switch.server.core.executor;

import pl.wieczorkep._switch.server.core.AudioPlayer;

import java.util.Properties;

public class SoundExecutor implements ActionExecutor {
    @Override
    public boolean execute(Properties arguments) throws InterruptedException {
        // ToDo: ... execute sound action
        AudioPlayer audioPlayer = new AudioPlayer();
        audioPlayer.main(arguments.getProperty("songPath"));

        return false;
    }
}
