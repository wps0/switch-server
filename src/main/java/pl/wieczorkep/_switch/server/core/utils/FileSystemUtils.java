package pl.wieczorkep._switch.server.core.utils;

import lombok.Cleanup;
import lombok.NonNull;
import pl.wieczorkep._switch.server.core.Action;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

public final class FileSystemUtils {
    private FileSystemUtils() {}


    public static Optional<File> createSpotifyActionFile(@NonNull Action fromAction, @NonNull File actionsDir) throws IOException {
        final String spotifySpecific = "# Spotify action specific\n";
        Optional<File> fileOptional = createActionFile(fromAction, actionsDir);
        File actionFile = fileOptional.orElseThrow();
        // Append to created actions config file spotify action specific parameters
        @Cleanup FileWriter writer = new FileWriter(actionFile, true);
        writer.append("#\n");
        writer.append(spotifySpecific);
        writer.append("# Duration of the playback (in minutes)\n");
        writer.append("#duration=5\n");

        writer.append("#\n");
        writer.append(spotifySpecific);
        writer.append("# Device id on which the action will be performed. If not supplied, the user’s currently\n");
        writer.append("#  active device is the target. Something like:\n");
        writer.append("#deviceId=t6f1yiBu3hjn2k\n");

        writer.append("#\n");
        writer.append(spotifySpecific);
        writer.append("# Spotify URI of the context to play. Valid contexts are albums, artists, playlists.\n");
        writer.append("#  If not supplied, playback will start from recently stopped place.\n");
        writer.append("#contextUri=spotify:album:1Je1IMUlBXcx1Fz0WE7oPT\n");

        writer.append("#\n");
        writer.append(spotifySpecific);
        writer.append("# Indicates from where in the context playback should start. Only available when context_uri\n");
        writer.append("#  corresponds to an album or playlist object.\n");
        writer.append("# Default: 0 (start from the beginning of a playlist).\n");
        writer.append("#offset=0\n");

        return fileOptional;
    }

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

        @Cleanup
        FileWriter actionWriter = new FileWriter(actionFile);

        actionWriter.write("# This is an example action file\n");
        actionWriter.write("# Each comment line starts with the '#' character\n");
        actionWriter.write("#\n");

        actionWriter.write("# Run the task at " + fromAction.getExecutionTime().getExecutionHour() + "\n");
        actionWriter.write("# Possible values: <0, 23>\n");
        actionWriter.write("#hour=" + fromAction.getExecutionTime().getExecutionHour() + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Run the at the specified minute\n");
        actionWriter.write("# Possible values: <0, 59>\n");
        actionWriter.write("#minute=" + fromAction.getExecutionTime().getExecutionMinute() + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Run your task on the specified days\n");
        actionWriter.write("# Possible values: {M, TU, W, TH, F, SA, SU}\n");
        actionWriter.write("#days=" + ActionUtils.encodeDays(fromAction.getExecutionTime().getExecutionDays()) + "\n");

        actionWriter.write("#\n");
        actionWriter.write("# Type of the action\n");
        actionWriter.write("# Possible values: " + Arrays.toString(Action.Type.values()).replace('[', '{').replace(']', '}') + "\n");
        actionWriter.write("#type=" + fromAction.getType().name() + "\n");
        actionWriter.write("#typeArguments=" + fromAction.getRawArguments() + "\n");

        // set safe permissions
        setFilePermissions(actionFile);
        return Optional.of(actionFile);
    }

    /**
     * Sets safe file permissions (-rw-------). Note that file for which the permissions should be set
     *  must exist beforehand.
     *
     * @param target File which has to have the permissions changed.
     * @return True, if all the operations succeeded, false otherwise.
     * @throws FileNotFoundException When specified file does not exist.
     */
    public static boolean setFilePermissions(@NonNull File target) throws FileNotFoundException {
        if (!target.exists()) {
            throw new FileNotFoundException(target.getAbsolutePath() + " does not exists");
        }
        boolean status = target.setReadable(false, false);
        status &= target.setWritable(false, false);
        status &= target.setExecutable(false, false);
        status &= target.setReadable(true, true);
        status &= target.setWritable(true, true);
        return status;
    }
}
