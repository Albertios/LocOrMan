package de.unimuenster.ifgi.locormandemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.nio.charset.Charset;

import de.unimuenster.ifgi.locormandemo.eventbus.IncomingNearbyMessageEvent;
import de.unimuenster.ifgi.locormandemo.eventbus.UpdateResponseReceivedEvent;
import de.unimuenster.ifgi.locormandemo.fragments.ExperimentFragment;
import de.unimuenster.ifgi.locormandemo.manipulations.Experiment;
import de.unimuenster.ifgi.locormandemo.manipulations.Manipulation;

public class AdminActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ExperimentFragment.OnListFragmentInteractionListener {

    private static final String TAG = AdminActivity.class.getSimpleName();

    // publish strategy
    private int TTL_IN_SECONDS_PUB = 5; // 5 seconds
    private final Strategy PUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS_PUB).build();
    // subscription strategy
    private int TTL_IN_SECONDS_SUB = 60 * 60; // Sixty minutes
    private final Strategy SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS_SUB).build();
    private GoogleApiClient mGoogleApiClient;

    private Experiment mUpdatedExperiment = null;

    private MessageListener mMessageListener;

    private MessageManager mMessageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        setExperimentFragment();

        // set on click listener
        Button startLoggingButton = (Button) findViewById(R.id.startLoggingButton);
        startLoggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                // send start logging message
                publishStartLoggingMessage();
            }
        });

        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                // publish current experiment



                if (mUpdatedExperiment != null) {
                    publishExperimentState(mUpdatedExperiment);
                }

            }
        });








        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                Log.i(TAG, new String(message.getContent()).trim());
                EventBus.getDefault().post(new IncomingNearbyMessageEvent(message));
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
            }
        };

        mMessageManager = new MessageManager();

        buildGoogleApiClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        // TODO: check why this needs to be put here
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                Log.i(TAG, new String(message.getContent()).trim());
                EventBus.getDefault().post(new IncomingNearbyMessageEvent(message));
            }


            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        mMessageManager.startListeningForMessagesWithoutCallback();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        mMessageManager.stopListeningForMessages();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Builds {@link GoogleApiClient}, enabling automatic lifecycle management using
     * {@link GoogleApiClient.Builder#enableAutoManage(FragmentActivity,
     * int, GoogleApiClient.OnConnectionFailedListener)}. I.e., GoogleApiClient connects in
     * {@link AppCompatActivity#onStart}, or if onStart() has already happened, it connects
     * immediately, and disconnects automatically in {@link AppCompatActivity#onStop}.
     */
    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
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

    private Message createMessage(String content) {
        return new Message(content.getBytes(Charset.forName("UTF-8")));
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

    @Override
    public void onListFragmentInteraction(Experiment updatedExperiment) {
        mUpdatedExperiment = updatedExperiment;
    }

    // not used
    private void publishManipulationState(Manipulation updatedManipulation) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            publish("{\"manipulationID\":\""+updatedManipulation.getId()+"\",\"active\":\""+updatedManipulation.getState()+"\"}");
        }
    }

    private void publishExperimentState(Experiment updatedExperiment) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            publish(updatedExperiment.getManipulationsStateJSONString());
        }
    }

    private void publishStartLoggingMessage() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //publish();
        }
    }

    private void setExperimentFragment() {
        // set the fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.adminFrameContainer, new ExperimentFragment());
        fragmentTransaction.commit();
    }

    // eventbus
    @Subscribe
    public void UpdateResponseReceivedEvent(UpdateResponseReceivedEvent updateResponseReceivedEvent) {
        Toast.makeText(this, "Manipulations updated!", Toast.LENGTH_SHORT).show();
    }

}
