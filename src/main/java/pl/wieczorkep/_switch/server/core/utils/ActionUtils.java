package pl.wieczorkep._switch.server.core.utils;

import java.time.DayOfWeek;

public final class ActionUtils {
    private ActionUtils() {}

    public static DayOfWeek decodeDay(String day) {
        switch (day.toUpperCase()) {
            case "M":
                return DayOfWeek.MONDAY;
            case "TU":
                return DayOfWeek.TUESDAY;
            case "W":
                return DayOfWeek.WEDNESDAY;
            case "TH":
                return DayOfWeek.THURSDAY;
            case "F":
                return DayOfWeek.FRIDAY;
            case "SA":
                return DayOfWeek.SATURDAY;
            case "SU":
                return DayOfWeek.SUNDAY;
            default:
                throw ExceptionFactory.createDateTimeException(day);
        }
    }

    public static String encodeDays(DayOfWeek[] days) {
        StringBuilder daysStringBuilder = new StringBuilder("{");

        for (DayOfWeek day : days) {
            switch (day) {
                case MONDAY:
                    daysStringBuilder.append("M");
                    break;
                case TUESDAY:
                    daysStringBuilder.append("TU");
                    break;
                case WEDNESDAY:
                    daysStringBuilder.append("W");
                    break;
                case THURSDAY:
                    daysStringBuilder.append("TH");
                    break;
                case FRIDAY:
                    daysStringBuilder.append("F");
                    break;
                case SATURDAY:
                    daysStringBuilder.append("SA");
                    break;
                case SUNDAY:
                    daysStringBuilder.append("SU");
                default:
            }
            daysStringBuilder.append(',');
        }

        return daysStringBuilder.substring(0, daysStringBuilder.length() - 1) + '}';
    }
}
