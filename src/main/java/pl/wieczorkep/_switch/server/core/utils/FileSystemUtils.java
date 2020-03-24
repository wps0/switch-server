package pl.wieczorkep._switch.server.core.utils;

import lombok.Cleanup;
import lombok.NonNull;
import pl.wieczorkep._switch.server.core.Action;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

public final class FileSystemUtils {
    private FileSystemUtils() {}

    /**
     * @param fromAction The action for which the file should be created.
     * @param actionsDir The directory where actions are stored.
     * @return Optional
     * @throws IOException When it is unable to create action's file
     */
    @NonNull
    public static Optional<File> createActionFile(@NonNull Action fromAction, @NonNull File actionsDir) throws IOException {
        File actionFile = new File(actionsDir, fromAction.getActionId());
        if (!actionFile.exists() && !actionFile.createNewFile()) {
            throw new IOException("Unable to create example actions file.");
        }

        @Cleanup FileWriter actionWriter = new FileWriter(actionFile);

        actionWriter.write("# This is an example actions file\n");
        actionWriter.write("# Each comment line starts with the '#' char\n");
        actionWriter.write("#\n");

        actionWriter.write("# Run your task at " + fromAction.getExecutionTime().getExecutionHour() + "\n");
        actionWriter.write("# Acceptable values: 0-23\n");
        actionWriter.write("#hour=" + fromAction.getExecutionTime().getExecutionHour() + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Run your task at the specific minute\n");
        actionWriter.write("# Acceptable values: 0-59\n");
        actionWriter.write("#minute=" + fromAction.getExecutionTime().getExecutionMinute() + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Run your task at the specified days\n");
        actionWriter.write("# Acceptable values: {M, TU, W, TH, F, SA, SU}\n");
        actionWriter.write("#days=" + ActionUtils.encodeDays(fromAction.getExecutionTime().getExecutionDays()) + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Type of the action\n");
        actionWriter.write("# Acceptable values: " + Arrays.toString(Action.Type.values()).replace('[', '{').replace(']', '}') + "\n");
        actionWriter.write("#type=" + fromAction.getType().name() + "\n"); // ToDo: test if this works correctly
        actionWriter.write("#typeArguments=" + fromAction.getRawArguments() + "\n");

        return Optional.of(actionFile);
    }
}
