package pl.wieczorkep._switch.server.config;

import org.junit.jupiter.api.*;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionTest {

    private Action action;

    @BeforeEach
    void setUp() {
        action = new Action(20, 20, new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}, Action.Type.PLAY_SOUND, "kozak", "test-action");
    }

    @Test
    void getRawArguments() {
        // given
        Action action1 = new Action(14, 00, new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}, Action.Type.PLAY_SOUND, "C:\\songs\\action.wav", "test-action");
        Action action2 = new Action(14, 00, new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}, Action.Type.PLAY_SOUND, "/home/songs/a.wav", "test-action");
        // when
        // then
        assertEquals("kozak", action.getRawArguments());
        assertEquals("C:\\songs\\action.wav", action1.getRawArguments());
        assertEquals("/home/songs/a.wav", action2.getRawArguments());
    }

    @Test
    @Disabled
    void getArguments() {
    }

    @Test
    @Disabled
    void compareTo() {
    }

    @Test
    @Disabled
    void testToString() {
    }

    @Test
    @Disabled
    void getActionId() {
    }
}
