package pl.wieczorkep._switch.server.config;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.wieczorkep._switch.server.config.Action.Type;

public final class ActionUtils {
    private ActionUtils() {}

    @SneakyThrows
    public static Action loadAction(File actionFile) {
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
                actionFile.getName()
        );
    }

    public static void loadActions(AppConfig appConfig) throws IOException {
        @Cleanup
        BufferedInputStream actionsInputStream = new BufferedInputStream(new FileInputStream(appConfig.get(AppConfig.ACTIONS_FILE)));

        Properties activeActions = new Properties();
        activeActions.load(actionsInputStream);

        String actions = (String) Optional.ofNullable(activeActions.get("active"))
                .orElse("");

        appConfig.putActions(Arrays.stream(actions.split(","))
                .filter(s -> s.length() > 0)
                .map(actionId -> actionId.trim() + ".action")
                .distinct()
                .map(actionFile -> loadAction(new File(appConfig.get(AppConfig.ACTIONS_DIR) + File.separatorChar + actionFile)))
                .collect(Collectors.toMap(Action::getActionId, Function.identity())));
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
