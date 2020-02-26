package pl.wieczorkep._switch.server.view;

public interface View {
    <T> void info(T message);

    void error(String message);

    void config(String message);

    void debug(String message);

    int readInt(String label);
}
