package pl.wieczorkep._switch.server.config;

import lombok.*;
import pl.wieczorkep._switch.server.config.extractor.ArgumentsExtractor;
import pl.wieczorkep._switch.server.config.extractor.SoundPathExtractor;

import java.time.DayOfWeek;

@EqualsAndHashCode
public class Action {
    @Getter
    private ExecutionTime executionTime;
    @Getter
    private Type type;
    @Getter
    private String typeArguments;
    @Getter
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

//    @Override
//    public int compareTo(@NotNull Action otherAction) {
//        int dayDifference = this.executionTime.executionDays
//    }

    // ToDo: Type for notifying admins via email, client app on the windows computer.
    @AllArgsConstructor
    public enum Type {
        PLAY_SOUND(new SoundPathExtractor());

        @Getter
        private ArgumentsExtractor argumentsExtractor;
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
