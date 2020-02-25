package pl.wieczorkep._switch.server.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Action {
    @Getter
    private byte executionHour;
    @Getter
    private byte executionMinute;
    /**
     * @see java.util.Calendar
     */
    @Getter
    private int[] executionDays;
    @Getter
    private Type type;
    @Getter
    private String targetFilePath;

    public Action(byte executionHour, byte executionMinute, int[] executionDays, Type type, String targetFilePath) {
        this.executionHour = executionHour;
        this.executionMinute = executionMinute;
        this.executionDays = executionDays;
        this.type = type;
        this.targetFilePath = targetFilePath;
    }

    enum Type {
        PLAY_SOUND;
    }
}
