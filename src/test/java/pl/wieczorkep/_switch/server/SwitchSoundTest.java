package pl.wieczorkep._switch.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SwitchSoundTest {

    @Test
    void shouldRunTheServer() {
        // given
        SoundServer soundServerMock = mock(SoundServer.class);
        SwitchSound switchSound = new SwitchSound(soundServerMock);
        // when
        switchSound.run();
        // then
        verify(soundServerMock, times(1)).run();
    }

    @Test
    void getServerInstance() {
        // given
        SoundServer soundServerMock = mock(SoundServer.class);
        SwitchSound switchSound = new SwitchSound(soundServerMock);
        // when
        Object result = switchSound.getSoundServerInstance();
        // then
        assertEquals(result, soundServerMock);
    }
}