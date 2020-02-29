package pl.wieczorkep._switch.server.utils.factory;

import java.time.format.DateTimeParseException;

public class ExceptionFactory {
    public static final String dayNotRecognised = "Day not recognised. Supported values: {M, TU, W, TH, F, SA, SU}";

    public static DateTimeParseException createDateTimeException(String data, int index) {
        return new DateTimeParseException(dayNotRecognised, data, index);
    }

    public static DateTimeParseException createDateTimeException(String data) {
        return createDateTimeException(data, -1);
    }
}
