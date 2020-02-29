package pl.wieczorkep._switch.server.config;

import lombok.Cleanup;
import pl.wieczorkep._switch.server.utils.ActionUtils;

import java.io.*;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Optional;

public class ActionFactory {

    public Optional<File> createActionFile(AppConfig appConfig) throws IOException {
        Action exampleAction = new Action(
                (byte) 8,
                (byte) 45,
                new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.SATURDAY},
                Action.Type.PLAY_SOUND,
                "/path/to/the/file.wav",
                "example.action.example"
        );
        return createActionFile(appConfig, exampleAction);
    }

    public Optional<File> createActionFile(AppConfig appConfig, Action fromAction) throws IOException {
        return createActionFile(fromAction, new File(appConfig.get(AppConfig.ACTIONS_DIR) + File.separatorChar + fromAction.getActionId()));
    }

    public Optional<File> createActionFile(Action fromAction, File file) throws IOException {
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Unable to create example actions file.");
        }

        @Cleanup FileWriter actionWriter = new FileWriter(file);

        actionWriter.write("# This is an example actions file\n");
        actionWriter.write("# Each comment line starts with the '#' char\n");
        actionWriter.write("#\n");

        actionWriter.write("# Run your task at " + fromAction.getExecutionTime().getExecutionHour() + "\n");
        actionWriter.write("# Acceptable values: 0-23\n");
        actionWriter.write("#hour=" + fromAction.getExecutionTime().getExecutionHour() + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Run your task at the specific minute\n");
        actionWriter.write("# Acceptable values: 0-23\n");
        actionWriter.write("#minute=" + fromAction.getExecutionTime().getExecutionMinute() + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Run your task at the specified days\n");
        actionWriter.write("# Acceptable values: {M, TU, W, TH, F, SA, SU}\n");
        actionWriter.write("#days=" + ActionUtils.encodeDays(fromAction.getExecutionTime().getExecutionDays()) + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Type of the action\n");
        actionWriter.write("# Acceptable values: " + Arrays.toString(Action.Type.values()).replace('[', '{').replace(']', '}') + "\n");
        actionWriter.write("#type=PLAY_SOUND\n");

        return Optional.of(file);
    }
}
