package de.unimuenster.ifgi.locormandemo;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.unimuenster.ifgi.locormandemo.eventbus.AppWantsToSendExperimentUpdateReceivedResponseEvent;
import de.unimuenster.ifgi.locormandemo.eventbus.IncomingNearbyMessageEvent;
import de.unimuenster.ifgi.locormandemo.eventbus.UpdateResponseReceivedEvent;
import de.unimuenster.ifgi.locormandemo.manipulations.LocationManipulation;

/**
 * Created by sven on 23.06.16.
 */
public class MessageManager implements JsonUtilityExperimentCallback {

    private MessageManagerCallback mCallbackObject;
    private JsonUtility mJsonUtility;

    public void startListeningForMessages(MessageManagerCallback callbackObject) {
        mCallbackObject = callbackObject;
        EventBus.getDefault().register(this);
        mJsonUtility = new JsonUtility(this);

    }

    public void startListeningForMessagesWithoutCallback() {
        EventBus.getDefault().register(this);
        mJsonUtility = new JsonUtility(this);
    }

    public void stopListeningForMessages() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onIncomingNearbyMessageEvent(IncomingNearbyMessageEvent incomingNearbyMessageEvent) {
        // handle incoming message
        Log.i("INCOMING", "incoming nearby message!");

        String messageString = new String(incomingNearbyMessageEvent.nearbyMessage.getContent());
        Log.i("INCOMING", messageString);
        mJsonUtility.parseMessage(messageString);

    }

    @Override
    public void experimentUpdateMessageParsed(HashMap<String, Boolean> manipulationStates) {
        if (mCallbackObject != null) {
            mCallbackObject.onExperimentLocationManipulationUpdatesReceived(manipulationStates);
            // fire event here: user app wants to send a positive response
            EventBus.getDefault().post(new AppWantsToSendExperimentUpdateReceivedResponseEvent(JsonUtility.generateExperimentUpdateResponse()));
        }
    }

    @Override
    public void experimentUpdateResponseParsed() {
        // fire event here: admin app needs to communicate that the other side got the manipulation update
        EventBus.getDefault().post(new UpdateResponseReceivedEvent());
    }
}
