package pl.wieczorkep._switch.server.integration.spotify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyMacrosTest {

    @Test
    void encodeTest() {
        assertEquals("{\"offset\":alamakocurr=a0,\"position_ms\":0}", SpotifyMacros.encode("offset=alamakocurr=a0&position_ms=0"));
        assertEquals("{\"offset\":0,\"position_ms\":0}", SpotifyMacros.encode("offset=0&position_ms=0"));
        assertEquals("{}", SpotifyMacros.encode(""));

        assertThrows(IllegalArgumentException.class, () -> SpotifyMacros.encode(null));
    }
}
