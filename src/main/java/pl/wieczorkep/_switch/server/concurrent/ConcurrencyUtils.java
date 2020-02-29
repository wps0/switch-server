package pl.wieczorkep._switch.server.concurrent;

class ConcurrencyUtils {
    private ConcurrencyUtils() {}

    public static String prettifyThreadName(Thread thread) {
        return thread.getClass().getSimpleName() + "-Thread-" + thread.getId();
    }
}
