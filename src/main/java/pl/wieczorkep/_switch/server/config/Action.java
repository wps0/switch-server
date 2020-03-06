package pl.wieczorkep._switch.server.config;

import lombok.*;
import pl.wieczorkep._switch.server.config.executor.ActionExecutor;
import pl.wieczorkep._switch.server.config.executor.SoundExecutor;
import pl.wieczorkep._switch.server.config.extractor.ArgumentsExtractor;
import pl.wieczorkep._switch.server.config.extractor.SoundPathExtractor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Properties;

@EqualsAndHashCode
public class Action {
    @Getter
    private ExecutionTime executionTime;
    @Getter
    private Type type;
    private String typeArguments;
    /**
     * Contains extracted arguments.
     * ToDo: add live reload support
     */
    private Properties arguments;
    /**
     * An action id specified by the user (generally the path to an action inside the action dir,
     * including '.action' file extension).
     */
    @Getter
    @NonNull
    private final String actionId;

    public Action(ExecutionTime executionTime, Type type, String typeArguments, String actionId) {
        this.executionTime = executionTime;
        this.type = type;
        this.typeArguments = typeArguments;
        this.actionId = actionId;
    }

    public Action(byte executionHour, byte executionMinute, DayOfWeek[] executionDays, Type type, String typeArguments, final @NonNull String actionId) {
        this(new ExecutionTime(executionHour, executionMinute, executionDays), type, typeArguments, actionId);
    }

    public String getRawArguments() {
        return typeArguments;
    }

    @NonNull
    public Properties getArguments() {
        if (arguments == null) {
            arguments = type.getArgumentsExtractor().extract(typeArguments)
                    .orElse(new Properties());
        }
        return arguments;
    }

//    @Override
//    public int compareTo(@NotNull Action otherAction) {
//        int dayDifference = this.executionTime.executionDays
//    }

    // ToDo: Type for notifying admins via email, client app on the windows computer.
    @AllArgsConstructor
    public enum Type {
        PLAY_SOUND(new SoundPathExtractor(), new SoundExecutor());

        @Getter
        private ArgumentsExtractor argumentsExtractor;

        @Getter
        private ActionExecutor actionExecutor;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class ExecutionTime {
        @Getter
        private final byte executionHour;
        @Getter
        private final byte executionMinute;
        /**
         * @see java.util.Calendar
         */
        @Getter
        @NonNull
        private final DayOfWeek[] executionDays;

        private Entry<DayOfWeek, Boolean> canExecuteCache;

        public boolean canExecuteToday() {
            DayOfWeek today = LocalDateTime.now().getDayOfWeek();

            if (canExecuteCache.getKey() == today) {
                return canExecuteCache.getValue();
            }

            for (DayOfWeek day : executionDays) {
                if (day == today) {
                    canExecuteCache = new SimpleEntry<>(day, true);
                    return true;
                }
            }

            canExecuteCache = new SimpleEntry<>(today, false);
            return false;
        }

        public DayOfWeek getClosestExecutionDay() {
            if (canExecuteToday()) {
                return LocalDateTime.now().getDayOfWeek();
            }

            DayOfWeek today = LocalDateTime.now().getDayOfWeek();
            DayOfWeek closestExecutionDay = null;
            int minNextDayDelay = Integer.MAX_VALUE;

            for (DayOfWeek day : executionDays) {
                if (day.getValue() < today.getValue()) {
                    int nextDayDelay = 7 - today.getValue() + day.getValue();

                    if (minNextDayDelay < nextDayDelay) {
                        minNextDayDelay = nextDayDelay;
                        closestExecutionDay = day;
                    }
                }
            }
            return closestExecutionDay;
        }

//        public static int calculateDeviation(ExecutionTime toTime) {
//            final LocalDateTime NOW = LocalDateTime.now();
//            final int CURRENT_YEAR = NOW.getYear();
//            final Month CURRENT_MONTH = NOW.getMonth();
//            final int CURRENT_DAY_OF_MONTH = NOW.getDayOfMonth();
//            final int CURRENT_HOUR = NOW.getDayOfMonth();
//            final int CURRENT_MINUTE = NOW.getDayOfMonth();
//
//            LocalDateTime currentTime = LocalDateTime.of(CURRENT_YEAR, CURRENT_MONTH, CURRENT_DAY_OF_MONTH, CURRENT_HOUR, CURRENT_MINUTE);
//
//            LocalDateTime executionTime = LocalDateTime.of(CURRENT_YEAR, CURRENT_MONTH, toTime.getClosestExecutionDay().getValue(), toTime.getExecutionHour(), toTime.getExecutionMinute());
//
//            return
//                    (CURRENT_DAY_OF_MONTH - toTime.getClosestExecutionDay().getValue()) * 86400
//                    + (CURRENT_HOUR - toTime.getExecutionHour())
//        }
    }
}
