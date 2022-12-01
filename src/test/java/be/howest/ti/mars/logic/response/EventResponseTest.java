package be.howest.ti.mars.logic.response;

import be.howest.ti.mars.logic.domain.response.EventResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EventResponseTest {
    @Test
    void getType() {
        EventResponse eventResponse = new EventResponse("type", null);

        Assertions.assertEquals("type", eventResponse.getType());
    }

    @Test
    void manageChannel() {
        EventResponse eventResponse = new EventResponse("type", null);

        eventResponse.setChannel("my-test-channel");

        Assertions.assertEquals("my-test-channel", eventResponse.getChannel());
    }
}
