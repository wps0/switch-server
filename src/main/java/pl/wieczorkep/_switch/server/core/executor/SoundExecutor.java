package pl.wieczorkep._switch.server.core.executor;

import lombok.Setter;
import pl.wieczorkep._switch.server.SoundServer;
import pl.wieczorkep._switch.server.core.Action;
import pl.wieczorkep._switch.server.core.AudioPlayer;

public class SoundExecutor implements IActionExecutor {
    @Setter
    private Action action;

    @Override
    public boolean execute(SoundServer server) throws InterruptedException {
        // ToDo: ... execute sound action
        AudioPlayer audioPlayer = new AudioPlayer();
        audioPlayer.main(action.getArguments().getProperty("songPath"));

        return false;
    }
}
