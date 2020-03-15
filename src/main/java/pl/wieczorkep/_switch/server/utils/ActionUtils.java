package pl.wieczorkep._switch.server.utils;

import lombok.Cleanup;
import lombok.SneakyThrows;
import pl.wieczorkep._switch.server.SwitchSound;
import pl.wieczorkep._switch.server.config.Action;
import pl.wieczorkep._switch.server.config.AppConfig;
import pl.wieczorkep._switch.server.utils.factory.ExceptionFactory;

import java.io.*;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.wieczorkep._switch.server.config.Action.Type;

public final class ActionUtils {
    private ActionUtils() {}

    public static void loadActions(AppConfig appConfig) throws IOException {
        appConfig.getView().info("Loading actions...");
        @Cleanup
        BufferedInputStream actionsInputStream = new BufferedInputStream(new FileInputStream(appConfig.get(AppConfig.ACTIONS_FILE)));

        Properties activeActions = new Properties();
        activeActions.load(actionsInputStream);

        String actions = (String) Optional.ofNullable(activeActions.get("active"))
                .orElse("");

        appConfig.putActions(Arrays.stream(actions.split(","))
                .filter(actionId -> actionId.length() > 0)
                .map(actionId -> actionId.trim() + ".action")
                .distinct()
                .map(actionFile -> loadAction(new File(appConfig.get(AppConfig.ACTIONS_DIR) + File.separatorChar + actionFile)))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    @SneakyThrows
    public static Action loadAction(File actionFile) {
        FileReader reader = new FileReader(actionFile);
        StringBuilder fileStringBuilder = new StringBuilder();

        int character;
        while ((character = reader.read()) != -1) {
            fileStringBuilder.append((char) character);
        }

        String fileContents = fileStringBuilder.toString().replace("\\", "\\\\");
        Properties actionProperties = new Properties();
        actionProperties.load(new StringReader(fileContents));

        try {
            return new Action(
                    Byte.parseByte(actionProperties.getProperty("hour", "0")),
                    Byte.parseByte(actionProperties.getProperty("minute", "0")),
                    Stream.of(actionProperties
                            .getProperty("days", "")
                            .replace('{', ' ')
                            .replace('}', ' ')
                            .split(","))
                            .map(String::trim)
                            .map(ActionUtils::decodeDay)
                            .distinct()
                            .toArray(DayOfWeek[]::new),
                    Type.valueOf(actionProperties.getProperty("type", "PLAY_SOUND")),
                    actionProperties.getProperty("typeArguments"),
                    actionFile.getName()
            );
        } catch (Exception e) {
            SwitchSound.getConfig().getView().error("Could not load " + actionFile.getName() + "; " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static DayOfWeek decodeDay(String day) {
        switch (day.toUpperCase()) {
            case "M":
                return DayOfWeek.MONDAY;
            case "TU":
                return DayOfWeek.TUESDAY;
            case "W":
                return DayOfWeek.WEDNESDAY;
            case "TH":
                return DayOfWeek.THURSDAY;
            case "F":
                return DayOfWeek.FRIDAY;
            case "SA":
                return DayOfWeek.SATURDAY;
            case "SU":
                return DayOfWeek.SUNDAY;
            default:
                throw ExceptionFactory.createDateTimeException(day);
        }
    }

    public static String encodeDays(DayOfWeek[] days) {
        StringBuilder daysStringBuilder = new StringBuilder("{");

        for (DayOfWeek day : days) {
            switch (day) {
                case MONDAY:
                    daysStringBuilder.append("M");
                    break;
                case TUESDAY:
                    daysStringBuilder.append("TU");
                    break;
                case WEDNESDAY:
                    daysStringBuilder.append("W");
                    break;
                case THURSDAY:
                    daysStringBuilder.append("TH");
                    break;
                case FRIDAY:
                    daysStringBuilder.append("F");
                    break;
                case SATURDAY:
                    daysStringBuilder.append("SA");
                    break;
                case SUNDAY:
                    daysStringBuilder.append("SU");
                default:
            }
            daysStringBuilder.append(',');
        }

        return daysStringBuilder.substring(0, daysStringBuilder.length() - 1) + '}';
    }
}
