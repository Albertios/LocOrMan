package de.unimuenster.ifgi.locormandemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.nio.charset.Charset;

/**
 * Created by sven on 21.06.16.
 */
public class CommunicationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = CommunicationManager.class.getSimpleName();

    private int TTL_IN_SECONDS_PUB = 10; // 10 seconds
    private int TTL_IN_SECONDS_SUB = 60 * 60; // Sixty minutes
    private final Strategy PUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS_PUB).build();
    private final Strategy SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS_SUB).build();

    private GoogleApiClient mGoogleApiClient;
    private Boolean connected;

    private MessageListener mMessageListener;

    private CommunicationManagerCallback callbackObject;

    public CommunicationManager() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                String nearbyMessageString = new String(message.getContent()).trim();
                //String nearbyMessagenew String(nearbyMessageString.getBytes(Charset.forName("UTF-8")));
                if (callbackObject != null) {
                    callbackObject.onMessageReceived(nearbyMessageString);
                }
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
            }
        };
    }

    public CommunicationManager(CommunicationManagerCallback callbackObject) {
        this();
        this.callbackObject = callbackObject;
    }

    public void startCommunication(Context ctx) {
        buildGoogleApiClient(ctx);
        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void stopCommunication() {
        callbackObject = null;
        mGoogleApiClient.disconnect();
        unsubscribe();
    }

    private void buildGoogleApiClient(Context ctx) {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void sendMessage(String messageString) {
        publish(messageString);
    }

    private void publish(String messageContent) {
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer publishing");
                        /*
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTriggerButton.setActivated(false);
                            }
                        });
                        */
                    }
                }).build();

        Nearby.Messages.publish(mGoogleApiClient, createMessage(messageContent), options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Published successfully.");
                        } else {
                            Log.i(TAG, "Could not publish, status = " + status);
                            //mPublishSwitch.setChecked(false);
                        }
                    }
                });
    }

    private Message createMessage(String content) {
        return new Message(content.getBytes(Charset.forName("UTF-8")));
    }

    private void subscribe() {
        Log.i(TAG, "Subscribing");
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer subscribing");
                    }
                }).build();

        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Subscribed successfully.");
                        } else {
                            Log.i(TAG, "Could not subscribe, status = " + status);
                        }
                    }
                });
    }

    /**
     * Stops subscribing to messages from nearby devices.
     */
    private void unsubscribe() {
        Log.i(TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG,"Connection suspended. Error code: "+i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Error while connecting to Google Play services: \" +\n" + connectionResult.getErrorMessage());
    }
}
