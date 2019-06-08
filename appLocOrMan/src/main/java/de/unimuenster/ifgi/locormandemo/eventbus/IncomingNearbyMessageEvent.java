package de.unimuenster.ifgi.locormandemo.eventbus;

import com.google.android.gms.nearby.messages.Message;

/**
 * Created by sven on 23.06.16.
 */
public class IncomingNearbyMessageEvent {
    public final Message nearbyMessage;

    public IncomingNearbyMessageEvent(Message nearbyMessage) {
        this.nearbyMessage = nearbyMessage;
    }
}
