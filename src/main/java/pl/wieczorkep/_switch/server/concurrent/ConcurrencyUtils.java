package pl.wieczorkep._switch.server.concurrent;

public class ConcurrencyUtils {
    private ConcurrencyUtils() {}

    public static String prettifyThreadName(Thread thread) {
        String[] splittedClassName = new Exception().getStackTrace()[1].getClassName().split("\\.");

        return splittedClassName[splittedClassName.length - 1] + "-Thread-" + thread.getId();
//        return splittedClassName[splittedClassName.length > 0 ? splittedClassName.length-1 : 1] + "-Thread-" + thread.getId();
    }
}
