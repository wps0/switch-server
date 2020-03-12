package pl.wieczorkep._switch.server.config;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import pl.wieczorkep._switch.server.config.executor.ActionExecutor;
import pl.wieczorkep._switch.server.config.executor.SoundExecutor;
import pl.wieczorkep._switch.server.config.extractor.ArgumentsExtractor;
import pl.wieczorkep._switch.server.config.extractor.SoundPathExtractor;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode
@ToString
public class Action implements Comparable<Action> {
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

    public Action(int executionHour, int executionMinute, DayOfWeek[] executionDays, Type type, String typeArguments, final @NonNull String actionId) {
        this(new ExecutionTime((byte) executionHour, (byte) executionMinute, executionDays), type, typeArguments, actionId);
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

    @Override
    public int compareTo(@NotNull Action o) {
        int exTimeCompare = executionTime.compareTo(o.getExecutionTime());
        if (exTimeCompare == 0) {
            return actionId.compareTo(o.getActionId());
        } else {
            return exTimeCompare;
        }
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
    public static class ExecutionTime implements Comparable<ExecutionTime> {
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

        public ExecutionTime(int executionHour, int executionMinute, DayOfWeek[] executionDays) {
            this((byte) executionHour, (byte) executionMinute, executionDays);
        }

        public boolean canExecuteToday(final LocalDateTime now) {
            DayOfWeek today = now.getDayOfWeek();


            if (canExecuteCache != null && canExecuteCache.getKey() == today) {

                if (now.getHour() < executionHour || (now.getHour() == executionHour && now.getMinute() < executionMinute)) {
                    return canExecuteCache.getValue();
                } else {
                    return false;
                }
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

        public DayOfWeek getClosestExecutionDay(final LocalDateTime now) {
            if (canExecuteToday(now)) {
                return now.getDayOfWeek();
            }

            if (executionDays == null) {
                return null;
            }

            DayOfWeek closestDay = null;
            int minPeriod = Integer.MAX_VALUE;

            for (DayOfWeek day : executionDays) {
                Period period = Period.between(now.toLocalDate(), now.with(TemporalAdjusters.nextOrSame(day)).toLocalDate());
                if (period.getDays() < minPeriod) {
                    minPeriod = period.getDays();
                    closestDay = day;
                }
            }

            return closestDay;
        }

        public long getTime(TimeUnit timeUnit) {
            return getTime(timeUnit, LocalDateTime.now());
        }

        /**
         * Gets amount of time to the next run.
         *
         * @param timeUnit the time value.
         * @param now      the current moment.
         * @return -1 if the closest day is null.
         */
        public long getTime(TimeUnit timeUnit, final LocalDateTime now) {
            final DayOfWeek closestDay = getClosestExecutionDay(now);

            if (closestDay == null) {
                return -1;
            }

            LocalDateTime nextExecution = now
                    .minusHours(now.getHour())
                    .minusMinutes(now.getMinute())
                    .minusSeconds(now.getSecond())
                    .with(TemporalAdjusters.nextOrSame(closestDay))
                    .plusHours(executionHour)
                    .plusMinutes(executionMinute);

            return timeUnit.convert(
                    ChronoUnit.MICROS.between(now, nextExecution),
                    TimeUnit.MICROSECONDS);
        }

        @Override
        public int compareTo(@NotNull Action.ExecutionTime o) {
            long executionTime = getTime(TimeUnit.MILLISECONDS);
            long oExecutionTime = o.getTime(TimeUnit.MILLISECONDS);

            System.out.println(executionTime + " & " + oExecutionTime);

            // todo: upewnic sie, ze to dziala
            return Long.compare(executionTime, oExecutionTime);
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
