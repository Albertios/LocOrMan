package de.unimuenster.ifgi.locormandemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import de.unimuenster.ifgi.locormandemo.eventbus.AppWantsToSendExperimentUpdateReceivedResponseEvent;
import de.unimuenster.ifgi.locormandemo.eventbus.IncomingNearbyMessageEvent;
import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationOrientationCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    private static String TAG = MapsActivity.class.getSimpleName();

    private LocationOrientationManager locOrMan;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Marker mMarker;
    private Circle mCircle;
    private Polyline mRoute;
    private boolean mFirstLocUpdateReceived = false;

    // for nearby communication
    private GoogleApiClient mGoogleApiClient;
    // subscription strategy
    private int TTL_IN_SECONDS_SUB = 60 * 60; // Sixty minutes
    private final Strategy SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS_SUB).build();
    // publish strategy
    private int TTL_IN_SECONDS_PUB = 5; // 5 seconds
    private final Strategy PUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS_PUB).build();

    private MessageListener mMessageListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //mapFragment.getActivity().onBackPressed();

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                Log.i(TAG, new String(message.getContent()).trim());
                //fireToast("Incoming message");
                EventBus.getDefault().post(new IncomingNearbyMessageEvent(message));
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
            }
        };

        buildGoogleApiClient();

        locOrMan = new LocationOrientationManager(this, this);
        Log.d(TAG, "onCreate()");
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
                //fireToast("Incoming message");
                EventBus.getDefault().post(new IncomingNearbyMessageEvent(message));
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        locOrMan.startLocationUpdates();
        locOrMan.startOrientationUpdates(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        locOrMan.stopLocationUpdates();
        locOrMan.stopOrientationUpdates(this);
        super.onStop();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setBuildingsEnabled(false);

        LatLng muenster = new LatLng(51.966354, 7.6067647);
        // load route
        drawRouteOnMapFromGpxTrack("Newroute.gpx");
        // draw marker
        mCircle = mMap.addCircle(initializeAccuracyBuffer().center(muenster));
        //mMarker = mMap.addMarker(new MarkerOptions().position(muenster));
        initCustomLocationMarker(muenster);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(muenster, 10.0f));
        mMarker.setVisible(false);
        mCircle.setVisible(false);
    }

    @Override
    public void onLocationChanged(Location newLocation) {

        LatLng currentLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());

        if (mFirstLocUpdateReceived == false) {
            mMarker.setVisible(true);
            mCircle.setVisible(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0f));
            mFirstLocUpdateReceived = true;
        }

        // update map
        mMarker.setPosition(currentLatLng);
        mCircle.setCenter(currentLatLng);
        mCircle.setRadius(newLocation.getAccuracy());

        // do only if user wants to be tracked
        // mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));

    }

    @Override
    public void onOrientationChanged(double newOrientation) {
        // update orientation
        if  (mMarker != null) {
            mMarker.setRotation((float) newOrientation);
        }
    }

    @Override
    public void onAskedForPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {






                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {


                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }

        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Exception while connecting to Google Play services: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended. Error code: " + i);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
        subscribe();
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
     * Methods that fires a short toast.
     * @param text is the text of the toast.
     */
    public void fireToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Create custom marker options for the buffer.
     * @return custom CircleOptions (semi transparent blue buffer similar to Google Maps)
     */
    private CircleOptions initializeAccuracyBuffer() {
        int mapsBlue = 0xFF0000FF;
        int mapsBlueTransparent = Color.argb(40,66,133,244);
        CircleOptions bufferOptions = new CircleOptions()
            .strokeColor(mapsBlue)
            .strokeWidth(1.5f)
            .fillColor(mapsBlueTransparent);
        return bufferOptions;
    }

    /**
     * Initialize the custom bitmap location marker.
     * @param latLng initial location for marker
     */
    private void initCustomLocationMarker(LatLng latLng) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(400, 400, conf);
        Canvas canvas1 = new Canvas(bmp);

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;

        Bitmap imageBitmap=BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_location_direction_blue,opt);
        Bitmap resized = Bitmap.createScaledBitmap(imageBitmap, 320, 320, true);
        canvas1.drawBitmap(resized, 40, 40, null);

        mMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 0.5f));
    }

    /**
     * Draw a route on the map
     * @param waypoints
     */
    private void drawRouteOnMap(ArrayList<LatLng> waypoints) {
        if (mMap == null || waypoints.size() < 2) {
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        //polylineOptions.color(Color.parseColor("#CC0000FF"));
        polylineOptions.color(Color.argb(180,66,100,244));
        polylineOptions.width(8.0f);
        polylineOptions.visible(true);

        for (LatLng waypoint : waypoints) {
            polylineOptions.add(waypoint);
        }

        mRoute = mMap.addPolyline(polylineOptions);
    }

    /**
     * Loads a GPX track from the assets.
     * @param filename of the GPX file in the assets folder
     * @return arraylist with elements of type LatLng containing the trackpoints of the GPX track.
     */
    private ArrayList<LatLng> loadWaypointsFromGpxFile(String filename) {
        GPXParser mParser = new GPXParser();
        ArrayList<LatLng> parsedWaypoints = new ArrayList<>();

        Gpx parsedGpx = null;
        try {
            InputStream in = getAssets().open(filename);
            parsedGpx = mParser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        if (parsedGpx != null) {
            // do something with the parsed track
            List<Track> parsedTracks = parsedGpx.getTracks();
            if (!parsedTracks.isEmpty() && parsedTracks.size() > 0) {
                Track firstTrack = parsedTracks.get(0);
                if (!firstTrack.getTrackSegments().isEmpty() && firstTrack.getTrackSegments().size() > 0) {
                    TrackSegment firstSegment = firstTrack.getTrackSegments().get(0);
                    List<TrackPoint> trackPoints = firstSegment.getTrackPoints();
                    for (TrackPoint tp : trackPoints) {
                        parsedWaypoints.add(new LatLng(tp.getLatitude(),tp.getLongitude()));
                    }
                }
            }
        }
        return parsedWaypoints;
    }

    /**
     * Draws a GPX track on the map.
     * @param filename of the GPX track in the assets that should be drawn on the map.
     */
    private void drawRouteOnMapFromGpxTrack(String filename) {
        ArrayList<LatLng> waypoints = loadWaypointsFromGpxFile(filename);
        drawRouteOnMap(waypoints);
    }


    @Override
    public void onBackPressed() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Quit?");
        alertDialog.setMessage("Do you really want to leave this screen?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MapsActivity.super.onBackPressed();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alertDialog.show();

    }

    @Subscribe
    public void onAppWantsToSendExperimentUpdateReceivedResponseEvent(AppWantsToSendExperimentUpdateReceivedResponseEvent appWantsToSendExperimentUpdateReceivedResponseEvent) {
        // send nearby message here
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            publish(appWantsToSendExperimentUpdateReceivedResponseEvent.messagePayloadString);
        }
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

}
