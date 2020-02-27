package pl.wieczorkep._switch.server.config;

import lombok.*;

import java.time.DayOfWeek;

@EqualsAndHashCode
public class Action {
    @Getter
    private ExecutionTime executionTime;
    @Getter
    private Type type;
    @Getter
    private final String actionId;

    public Action(byte executionHour, byte executionMinute, DayOfWeek[] executionDays, Type type, final @NonNull String actionId) {
        this.executionTime = new ExecutionTime(executionHour, executionMinute, executionDays);
        this.type = type;
        this.actionId = actionId;
    }

//    @Override
//    public int compareTo(@NotNull Action otherAction) {
//        int dayDifference = this.executionTime.executionDays
//    }

    public enum Type {
        PLAY_SOUND;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class ExecutionTime {
        @Getter
        private byte executionHour;
        @Getter
        private byte executionMinute;
        /**
         * @see java.util.Calendar
         */
        @Getter
        private DayOfWeek[] executionDays;
    }
}
