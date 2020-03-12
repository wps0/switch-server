package pl.wieczorkep._switch.server.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.wieczorkep._switch.server.utils.factory.ExceptionFactory;

import java.time.DayOfWeek;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ActionUtilsTest {
    private static final Properties days = new Properties();

    @BeforeAll
    static void beforeAll() {
        days.setProperty("M", "MONDAY");
        days.setProperty("TU", "TUESDAY");
        days.setProperty("W", "WEDNESDAY");
        days.setProperty("TH", "THURSDAY");
        days.setProperty("F", "FRIDAY");
        days.setProperty("SA", "SATURDAY");
        days.setProperty("SU", "SUNDAY");
    }

    @ParameterizedTest(name = "Day code {0}")
    @ValueSource(strings = {"M", "TU", "W", "TH", "F", "SA", "SU", "tu"})
    void decodeDay_shouldReturnValidDay(String day) {
        // when
        String returnedDay = String.valueOf(ActionUtils.decodeDay(day));
        // then
        assertEquals(Optional.ofNullable(days.get(day.toUpperCase())).orElseThrow(() -> ExceptionFactory.createDateTimeException(day)), returnedDay);
    }

    @ParameterizedTest(name = "Day code {0}")
    @ValueSource(strings = {"Ma", "fr", "sunday", "\\", "\\n"})
    void decodeDay_shouldThrowException(String day) {
        // then
        assertThrows(DateTimeParseException.class, () -> ActionUtils.decodeDay(day));
    }

    @Test
    void encodeDay_shouldReturnValidDayString() {
        DayOfWeek[] days = {
                DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.SUNDAY, DayOfWeek.THURSDAY,
                DayOfWeek.TUESDAY, DayOfWeek.SATURDAY, DayOfWeek.WEDNESDAY
        };
        // when
        String returnedDay = ActionUtils.encodeDays(days);
        // then
        assertEquals("{F,M,SU,TH,TU,W}", returnedDay);
    }

    @Test
    @Disabled
    void loadActions() {
        fail("Not yet implemented");
    }

    @Test
    @Disabled
    void loadAction() {
        fail("Not yet implemented");
    }
}
