package pl.wieczorkep._switch.server.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.wieczorkep._switch.server.config.Action.ExecutionTime;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ActionExecutionTimeTest {

    @ParameterizedTest(name = "ExecutionTime: {0}; Now: {1}")
    @MethodSource("presentTimeProvider")
    void canExecuteToday(final ExecutionTime executionTime, final LocalDateTime NOW) {
        // given
        List<DayOfWeek> dayOfWeekList = Arrays.asList(executionTime.getExecutionDays());

        // when
        boolean canExecute = executionTime.canExecuteToday(NOW);

        // then
        assertEquals(dayOfWeekList.contains(NOW.getDayOfWeek()), canExecute);
    }

    @Test
    @Disabled
    void getTime() {
        fail("not yet implemented");
    }


    @ParameterizedTest(name = "ExecutionTime: {0} & {1}?")
    @MethodSource("executionTimeProvider")
    void compareTo(ExecutionTime x, ExecutionTime y, int correctXtoY) {
        // given
        // when
        // then
        assertEquals(correctXtoY, x.compareTo(y));
        assertEquals(-correctXtoY, y.compareTo(x));
    }

    ///////////////////////////////////////////////////////////////////////////
    // providers
    ///////////////////////////////////////////////////////////////////////////
    private static Stream<Arguments> presentTimeProvider() {
        return Stream.of(
                Arguments.of(
                        new ExecutionTime((byte) 20, (byte) 47, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY}),
                        /* 10.10.2020 - SATURDAY */
                        LocalDateTime.of(2020, 10, 10, 16, 30, 21)
                ),
                Arguments.of(
                        new ExecutionTime((byte) 11, (byte) 0, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY}),
                        /* 10.10.2020 - SUNDAY */
                        LocalDateTime.of(2018, 8, 9, 2, 33, 54)
                ),
                Arguments.of(
                        new ExecutionTime((byte) 9, (byte) 45, new DayOfWeek[]{}),
                        /* 10.10.2020 - SUNDAY */
                        LocalDateTime.of(2018, 8, 9, 2, 33, 54)
                ),
                Arguments.of(
                        new ExecutionTime((byte) 7, (byte) 0, new DayOfWeek[]{DayOfWeek.MONDAY}),
                        /* 01.01.1996 - MONDAY */
                        LocalDateTime.of(1996, 1, 1, 0, 0, 0)
                ),
                Arguments.of(
                        new ExecutionTime((byte) 7, (byte) 0, new DayOfWeek[]{DayOfWeek.SUNDAY}),
                        /* 01.01.1996 - MONDAY */
                        LocalDateTime.of(1996, 1, 1, 0, 0, 0)
                )
        );
    }

    private static Stream<Arguments> executionTimeProvider() {
        return Stream.of(
                Arguments.of(
                        new ExecutionTime(20, 47, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY}),
                        new ExecutionTime(1, 2, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY}),
                        1
                ),
                Arguments.of(
                        new ExecutionTime(23, 59, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.SATURDAY}),
                        new ExecutionTime(0, 0, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.SATURDAY}),
                        1
                ),
                Arguments.of(
                        new ExecutionTime(20, 47, new DayOfWeek[]{DayOfWeek.FRIDAY, DayOfWeek.SATURDAY}),
                        new ExecutionTime(20, 47, new DayOfWeek[]{DayOfWeek.FRIDAY, DayOfWeek.SATURDAY}),
                        0
                ),
                Arguments.of(
                        new ExecutionTime(11, 11, new DayOfWeek[]{DayOfWeek.THURSDAY}),
                        new ExecutionTime(11, 11, new DayOfWeek[]{DayOfWeek.THURSDAY}),
                        0
                ),
                Arguments.of(
                        new ExecutionTime(0, 0, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY}),
                        new ExecutionTime(0, 1, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY}),
                        -1
                ),
                Arguments.of(
                        new ExecutionTime(23, 58, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.SATURDAY}),
                        new ExecutionTime(23, 59, new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.SATURDAY}),
                        -1
                )
        );
    }
}
