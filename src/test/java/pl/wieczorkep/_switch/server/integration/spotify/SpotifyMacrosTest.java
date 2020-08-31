package pl.wieczorkep._switch.server.integration.spotify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyMacrosTest {

    @Test
    void encodeTest() {
        assertEquals("{\"offset\":{\"alamakocurr\":\"a0\"},\"position_ms\":\"0\"}", SpotifyMacros.encode("offset=alamakocurr=a0&position_ms=0"));
        assertEquals("{\"offset\":\"0\",\"position_ms\":\"0\"}", SpotifyMacros.encode("offset=0&position_ms=0"));
        assertEquals("{}", SpotifyMacros.encode(""));
        assertEquals("{\"embedded\":{\"embedded1\":\"embed2\"},\"emb1\":{\"emb2\":\"emb3\"},\"siema\":\"1\"}",
                SpotifyMacros.encode("embedded=embedded1=embed2&emb1=emb2=emb3&siema=1"));

        assertThrows(IllegalArgumentException.class, () -> SpotifyMacros.encode(null));
    }
}
