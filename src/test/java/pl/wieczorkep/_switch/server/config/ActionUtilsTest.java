package pl.wieczorkep._switch.server.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionUtilsTest {

    @ParameterizedTest(name = "{0} = {1}")
    @ValueSource(strings = {"M", "TU", "W", "TH", "F", "SA", "SU", "siemamdads", "tu"})
    void decodeDay(String day) {
        // given
        Properties days = new Properties();
        days.setProperty("M", "2");
        days.setProperty("TU", "3");
        days.setProperty("W", "4");
        days.setProperty("TH", "5");
        days.setProperty("F", "6");
        days.setProperty("SA", "7");
        days.setProperty("SU", "1");

        // when
        String returnedDay = String.valueOf(ActionUtils.decodeDay(day));

        // then
        assertEquals(
                Optional.ofNullable(
                        days.get(day.toUpperCase())).orElse("-1"), returnedDay);
        System.out.println(Optional.ofNullable(days.get(day)).orElse("-1") + " -- "+ returnedDay);
    }
}
