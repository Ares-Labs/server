package be.howest.ti.mars.web.bridge;

import be.howest.ti.mars.logic.domain.EventHandler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * The RTC bridge is one of the class taught topics.
 * If you do not choose the RTC topic you don't have to do anything with this class.
 * Otherwise, you will need to expand this bridge with the websockets topics shown in the other modules.
 * <p>
 * The client-side starter project does not contain any teacher code about the RTC topic.
 * The rtc bridge is already initialized and configured in the WebServer.java.
 * No need to change the WebServer.java
 * <p>
 * The job of the "bridge" is to bridge between websockets events and Java (the controller).
 * Just like in the openapi bridge, keep business logic isolated in the package logic.
 */
public class MarsRtcBridge {
    private static final String OUTBOUND = "events.to.martians";
    private static final String INBOUND = "events.from.martians";
    private SockJSHandler sockJSHandler;
    private EventBus eb;

    private void createSockJSHandler() {
        final PermittedOptions permittedOptions = new PermittedOptions().setAddressRegex("events\\..+");
        final SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(permittedOptions)
                .addOutboundPermitted(permittedOptions);

        sockJSHandler.bridge(options);
    }

    private void handleConsumerMessage(String address, Message<String> msg) {

        System.out.println(new JsonObject(msg.body()));
        System.out.println(new JsonObject(msg.body()).getJsonObject("data"));

        EventHandler eh = EventHandler.getInstance();
        SocketResponse response = eh.handleIncomingEvent(new JsonObject(msg.body()));
//        String res = response.toMessage().toString();
        eb.publish(address, response.toMessage());
    }

    private void handlePublicConsumerMessage(Message<String> msg) {
        handleConsumerMessage(OUTBOUND, msg);
    }

    public SockJSHandler getSockJSHandler(Vertx vertx) {
        sockJSHandler = SockJSHandler.create(vertx);
        eb = vertx.eventBus();
        createSockJSHandler();

        eb.consumer(INBOUND, this::handlePublicConsumerMessage);

        // Session event bus initialisation
        EventHandler.getInstance().addEventHandler("session", data -> {
            String id = data.toString();
            eb.consumer(INBOUND + "." + id, (Message<String> msg) -> handleConsumerMessage(OUTBOUND + "." + id, msg));
            return null;
        });

        return sockJSHandler;
    }
}
