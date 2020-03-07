package pl.wieczorkep._switch.server.view;

public interface View {
    <T> void info(T message);

    void error(String message);

    void config(String message);

    <T> void debug(T message);

    void debug(Thread thread, String message);

    int readInt(String label);

    void init();
}
