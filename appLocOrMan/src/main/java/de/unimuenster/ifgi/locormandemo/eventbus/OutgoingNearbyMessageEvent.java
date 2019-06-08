package de.unimuenster.ifgi.locormandemo.eventbus;

/**
 * Created by sven on 23.06.16.
 */
public class OutgoingNearbyMessageEvent {
    public final String messagePayload;

    public OutgoingNearbyMessageEvent(String messagePayloadString) {
        this.messagePayload = messagePayloadString;
    }
}
