package pl.wieczorkep._switch.server.utils;

import lombok.Cleanup;
import lombok.SneakyThrows;
import pl.wieczorkep._switch.server.SwitchSound;
import pl.wieczorkep._switch.server.config.Action;
import pl.wieczorkep._switch.server.config.AppConfig;

import java.io.*;
import java.time.DayOfWeek;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.wieczorkep._switch.server.config.Action.Type;

public final class ActionUtils {
    private ActionUtils() {}

    public static void loadActions(AppConfig appConfig) throws IOException {
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
                .collect(Collectors.toMap(Action::getActionId, Function.identity())));
    }

    @SneakyThrows
    public static Action loadAction(File actionFile) {
        Properties actionProperties = new Properties();
        actionProperties.load(new FileInputStream(actionFile));

        try {
            return new Action(
                    Byte.parseByte(actionProperties.getProperty("hour", "0")),
                    Byte.parseByte(actionProperties.getProperty("minute", "0")),
                    (DayOfWeek[]) Stream.of(actionProperties
                            .getProperty("days", "")
                            .split(","))
                            .map(String::trim)
                            .map(ActionUtils::decodeDay)
                            .distinct()
                            .toArray(),
                    Type.valueOf(actionProperties.getProperty("type", "PLAY_SOUND")),
                    actionProperties.getProperty("typeArguments"),
                    actionFile.getName()
            );
        } catch (Exception e) {
            SwitchSound.getConfig().getView().error("Could not load " + actionFile.getName() + "; " + e.getLocalizedMessage());
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
                throw new DateTimeParseException("Day not recognised. Supported values: {M, TU, W, TH, F, SA, SU}", day, -1);
        }
    }
}
