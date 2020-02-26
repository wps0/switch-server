package pl.wieczorkep._switch.server.config;

import lombok.*;

@EqualsAndHashCode
public class Action {
    private ExecutionTime executionTime;
    @Getter
    private Type type;
    @Getter
    private final String actionId;

    public Action(byte executionHour, byte executionMinute, int[] executionDays, Type type, final String actionId) {
        this.executionTime = new ExecutionTime(executionHour, executionMinute, executionDays);
        this.type = type;
        this.actionId = actionId;
    }

    enum Type {
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
        private int[] executionDays;
    }
}
