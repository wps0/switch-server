package pl.wieczorkep._switch.server.core.utils.factory;

import java.time.format.DateTimeParseException;

public class ExceptionFactory {
    public static final String UNKNOWN_DAY = "Day not recognised. Supported values: {M, TU, W, TH, F, SA, SU}";

    private ExceptionFactory() {}

    public static DateTimeParseException createDateTimeException(String data, int index) {
        return new DateTimeParseException(UNKNOWN_DAY, data, index);
    }

    public static DateTimeParseException createDateTimeException(String data) {
        return createDateTimeException(data, -1);
    }
}
