package pl.wieczorkep._switch.server.config;

import java.util.Calendar;

public final class ActionUtils {
    private ActionUtils() {}
//
//    public static Action loadAction(File actionFile) throws IOException {
//        Properties actionProperties = new Properties();
//        actionProperties.load(new FileInputStream(actionFile));
//
//
//
//        return new Action(
//                Byte.parseByte(actionProperties.getProperty("hour", "0")),
//                Byte.parseByte(actionProperties.getProperty("minute", "0")),
//
//                );
//    }

    public static int decodeDay(String day) {
        switch (day.toUpperCase()) {
            case "M":
                return Calendar.MONDAY;
            case "TU":
                return Calendar.TUESDAY;
            case "W":
                return Calendar.WEDNESDAY;
            case "TH":
                return Calendar.THURSDAY;
            case "F":
                return Calendar.FRIDAY;
            case "SA":
                return Calendar.SATURDAY;
            case "SU":
                return Calendar.SUNDAY;
            default:
                return -1;
        }
    }
}
