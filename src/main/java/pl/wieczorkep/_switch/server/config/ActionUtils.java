package pl.wieczorkep._switch.server.config;

import java.io.*;
import java.util.Calendar;
import java.util.Properties;
import java.util.stream.Stream;

import static pl.wieczorkep._switch.server.config.Action.Type;

public final class ActionUtils {
    private ActionUtils() {}

    public static Action loadAction(File actionFile) throws IOException {
        Properties actionProperties = new Properties();
        actionProperties.load(new FileInputStream(actionFile));

        return new Action(
                Byte.parseByte(actionProperties.getProperty("hour", "0")),
                Byte.parseByte(actionProperties.getProperty("minute", "0")),
                Stream.of(actionProperties
                        .getProperty("days", "")
                        .split(","))
                        .mapToInt(ActionUtils::decodeDay)
                        .distinct()
                        .toArray(),
                Type.valueOf(actionProperties.getProperty("type", "PLAY_SOUND")),
                actionFile.getAbsolutePath()
        );
    }

    public static int decodeDay(String day) {
        switch (day.toUpperCase()) {
            case "M":
                return Calendar.MONDAY;
            case "TU":
                return Calendar.TUESDAY;
            case "W":
                return Calendar.WEDNESDAY;
            case "TH":
                return Calendar.THURSDAY;
            case "F":
                return Calendar.FRIDAY;
            case "SA":
                return Calendar.SATURDAY;
            case "SU":
                return Calendar.SUNDAY;
            default:
                return -1;
        }
    }
}
