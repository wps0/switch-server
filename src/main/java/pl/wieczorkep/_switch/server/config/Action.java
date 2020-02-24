package pl.wieczorkep._switch.server.config;

public class Action {
    private byte executionHour;
    private byte executionMinute;
    /**
     * @see java.util.Calendar
     */
    private int[] executionDays;
    private Type type;
    private String targetFilePath;

    public Action(byte executionHour, byte executionMinute, int[] executionDays, Type type, String targetFilePath) {
        this.executionHour = executionHour;
        this.executionMinute = executionMinute;
        this.executionDays = executionDays;
        this.type = type;
        this.targetFilePath = targetFilePath;
    }

    public byte getExecutionHour() {
        return executionHour;
    }

    public byte getExecutionMinute() {
        return executionMinute;
    }

    public int[] getExecutionDays() {
        return executionDays;
    }

    public Type getType() {
        return type;
    }

    public String getTargetFilePath() {
        return targetFilePath;
    }

    enum Type {
        PLAY_SOUND;
    }
}
