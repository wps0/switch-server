package pl.wieczorkep._switch.server.core;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import pl.wieczorkep._switch.server.core.executor.*;
import pl.wieczorkep._switch.server.core.extractor.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class Action implements Comparable<Action> {
    /**
     * An action id specified by the user (generally the path to an action inside the action dir,
     * including '.action' file extension).
     */
    @Getter @NonNull
    private final String actionId;
    @Getter
    private final ExecutionTime executionTime;
    @Getter
    private final Type type;
    private final String typeArguments;
    /**
     * Contains extracted arguments.
     * TODO: add live reload support
     */
    private Properties arguments;

    public Action(int executionHour, int executionMinute, DayOfWeek[] executionDays, Type type, String typeArguments, final @NonNull String actionId) {
        this(actionId, new ExecutionTime(executionHour, executionMinute, executionDays), type, typeArguments);
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


    // TODO: Email notifications, client app notifications
    @AllArgsConstructor
    public enum Type {
        PLAY_SOUND(new SoundPathExtractor(), new SoundExecutor()),
        SPOTIFY_PLAY_PLAYLIST(new SpotifyPlaylistExtractor(), new SpotifyPlaylistExecutor());

        @Getter
        private final ArgumentsExtractor argumentsExtractor;
        @Getter
        private final ActionExecutor actionExecutor;
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
        @Getter @NonNull
        private final DayOfWeek[] executionDays;

        public ExecutionTime(int executionHour, int executionMinute, @NonNull DayOfWeek[] executionDays) {
            this((byte) executionHour, (byte) executionMinute, executionDays);
        }

        public boolean canExecuteToday(final LocalDateTime now) {
            DayOfWeek today = now.getDayOfWeek();

            if (now.getHour() < executionHour || (now.getHour() == executionHour && now.getMinute() < executionMinute)) {
                for (DayOfWeek day : executionDays) {
                    if (day == today) {
                        return true;
                    }
                }
            }

            return false;
        }

        public DayOfWeek getClosestExecutionDay(final LocalDateTime now) {
            if (canExecuteToday(now)) {
                return now.getDayOfWeek();
            }

            DayOfWeek closestDay = null;
            int minPeriod = Integer.MAX_VALUE;

            for (DayOfWeek day : executionDays) {
                Period period = Period.between(now.toLocalDate(), now.with(TemporalAdjusters.next(day)).toLocalDate());
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

            LocalDateTime todayBlank = now
                    .minusHours(now.getHour())
                    .minusMinutes(now.getMinute())
                    .minusSeconds(now.getSecond());

            LocalDateTime nextExecution = todayBlank
                    .with(TemporalAdjusters.nextOrSame(closestDay))
                    .plusHours(executionHour)
                    .plusMinutes(executionMinute);

            long millisToNextExecution = ChronoUnit.MILLIS.between(now, nextExecution);

            if (executionDays.length == 1 && millisToNextExecution < 0) {
                nextExecution = todayBlank
                        .with(TemporalAdjusters.next(closestDay))
                        .plusHours(executionHour)
                        .plusMinutes(executionMinute);
                millisToNextExecution = ChronoUnit.MILLIS.between(now, nextExecution);
            }

            return timeUnit.convert(millisToNextExecution, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NotNull Action.ExecutionTime o) {
            final long referenceValue = getReferenceTime();
            long executionTime = referenceValue + getTime(TimeUnit.MILLISECONDS);
            long oExecutionTime = referenceValue + o.getTime(TimeUnit.MILLISECONDS);

            // todo: upewnic sie, ze to dziala
            return Long.compare(executionTime, oExecutionTime);
        }

        public static long getReferenceTime() {
            return Instant.now().getEpochSecond();
        }
    }
}
