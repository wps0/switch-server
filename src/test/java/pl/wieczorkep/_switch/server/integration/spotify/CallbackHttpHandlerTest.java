package pl.wieczorkep._switch.server.integration.spotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;

import static org.junit.jupiter.api.Assertions.*;

class CallbackHttpHandlerTest {

    @Test
    @Timeout(1)
    void shouldFindResponseForCode() {
        // given
        CallbackHttpHandler handler = new CallbackHttpHandler(null);
        SimpleEntry<Integer, String>[] codes = CallbackHttpHandler.getResponseCodes();
        // when
        for (SimpleEntry<Integer, String> e : codes) {
            // then
            assertTimeout(Duration.ofMillis(75), () -> assertEquals(e.getValue(), handler.findResponseForCode(e.getKey())));
        }
    }
}