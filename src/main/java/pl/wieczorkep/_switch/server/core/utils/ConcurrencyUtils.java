package pl.wieczorkep._switch.server.core.utils;

public class ConcurrencyUtils {
    private ConcurrencyUtils() {}

    public static String prettifyThreadName(Thread thread) {
        String[] splitClassName = new Exception().getStackTrace()[1].getClassName().split("\\.");

        return splitClassName[splitClassName.length - 1] + "-Thread-" + thread.getId();
    }
}
