package pl.wieczorkep._switch.server.core.utils;

import lombok.NonNull;
import pl.wieczorkep._switch.server.core.Action;
import pl.wieczorkep._switch.server.core.AppConfig;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.Optional;

import static pl.wieczorkep._switch.server.Constants.ACTIONS_DIR;

public class ActionFactory {
    public static Action createExampleAction() {
        return new Action(
                8,
                45,
                new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.SATURDAY},
                Action.Type.PLAY_SOUND,
                "/path/to/the/file.wav",
                "example.action.example"
        );
    }

    public Optional<File> createActionFile(AppConfig appConfig) throws IOException {
        return createActionFile(createExampleAction(), new File(appConfig.get(ACTIONS_DIR)));
    }

    /**
     * @param fromAction The action for which the file should be created.
     * @param actionsDir The directory where actions are stored.
     * @return Optional
     * @throws IOException When it is unable to create action's file
     * @deprecated ?
     */
    @NonNull
    @Deprecated
    public Optional<File> createActionFile(@NonNull Action fromAction, @NonNull File actionsDir) throws IOException {
        return FileSystemUtils.createActionFile(fromAction, actionsDir);
    }
}
