package pl.wieczorkep._switch.server.core.utils;

import pl.wieczorkep._switch.server.core.Action;
import pl.wieczorkep._switch.server.core.AppConfig;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.Optional;

import static pl.wieczorkep._switch.server.Constants.ACTIONS_DIR;

public class ActionFactory {
    public static Action createExampleSpotifyAction() {
        return new Action(
                7,
                45,
                new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.SATURDAY},
                Action.Type.SPOTIFY_PLAY,
                "/path/to/the/file.wav",
                "spotify-action.example"
        );
    }

    public static Action createExampleAction() {
        return new Action(
                8,
                45,
                new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.SATURDAY},
                Action.Type.PLAY_SOUND,
                "/path/to/the/file.wav",
                "action.example"
        );
    }

    public Optional<File> createActionFile(AppConfig appConfig) throws IOException {
        return FileSystemUtils.createActionFile(createExampleAction(), new File(appConfig.get(ACTIONS_DIR)));
    }
}
