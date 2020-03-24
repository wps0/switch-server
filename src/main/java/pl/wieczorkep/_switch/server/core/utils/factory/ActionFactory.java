package pl.wieczorkep._switch.server.core.utils.factory;

import lombok.NonNull;
import pl.wieczorkep._switch.server.core.Action;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.core.utils.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.Optional;

public class ActionFactory {
    public static Action createExampleAction() {
        return new Action(
                (byte) 8,
                (byte) 45,
                new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.SATURDAY},
                Action.Type.PLAY_SOUND,
                "/path/to/the/file.wav",
                "example.action.example"
        );
    }

    public Optional<File> createActionFile(AppConfig appConfig) throws IOException {
        return createActionFile(createExampleAction(), new File(appConfig.get(AppConfig.ACTIONS_DIR)));
    }

    /**
     * @param fromAction The action for which the file should be created.
     * @param actionsDir The directory where actions are stored.
     * @return Optional
     * @throws IOException When it is unable to create action's file
     */
    @NonNull
    @Deprecated
    public Optional<File> createActionFile(@NonNull Action fromAction, @NonNull File actionsDir) throws IOException {
        return FileSystemUtils.createActionFile(fromAction, actionsDir);
    }
}
