package be.howest.ti.mars.logic.response;

import be.howest.ti.mars.logic.domain.response.EventResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class EventResponseTest {
    private final Map<String, Object> data = Map.of("key", "value");

    @Test
    void nullEntries() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EventResponse(null, data));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EventResponse("type", null));
    }

    @Test
    void getType() {
        EventResponse eventResponse = new EventResponse("type", data);

        Assertions.assertEquals("type", eventResponse.getType());
    }

    @Test
    void manageChannel() {
        EventResponse eventResponse = new EventResponse("type", data);

        eventResponse.setChannel("my-test-channel");

        Assertions.assertEquals("my-test-channel", eventResponse.getChannel());

    }
}
