package pl.wieczorkep._switch.server.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Action action1 = new Action(14, 0, new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}, Action.Type.PLAY_SOUND, "C:\\songs\\action.wav", "test-action");
        Action action2 = new Action(14, 0, new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}, Action.Type.PLAY_SOUND, "/home/songs/a.wav", "test-action");
        // when
        // then
        assertEquals("kozak", action.getRawArguments());
        assertEquals("C:\\songs\\action.wav", action1.getRawArguments());
        assertEquals("/home/songs/a.wav", action2.getRawArguments());
    }
}
