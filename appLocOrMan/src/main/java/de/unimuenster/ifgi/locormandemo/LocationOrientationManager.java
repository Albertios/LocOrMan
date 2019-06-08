package de.unimuenster.ifgi.locormandemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.HashMap;

import de.unimuenster.ifgi.locormandemo.eventbus.OrientationUpdateEvent;
import de.unimuenster.ifgi.locormandemo.filter.LocationFilter;
import de.unimuenster.ifgi.locormandemo.filter.Orientation;
import de.unimuenster.ifgi.locormandemo.filter.OrientationFilter;
import de.unimuenster.ifgi.locormandemo.manipulations.Experiment;
import de.unimuenster.ifgi.locormandemo.manipulations.ExperimentProvider;

/**
 * Created by sven on 17.06.16.
 */
public class LocationOrientationManager implements MessageManagerCallback {

    private LocationOrientationCallback callbackObject;
    private LocationManager androidLocationManager;
    private Context mContext;
    private final String TAG = LocationOrientationManager.class.getSimpleName();

    private MessageManager mMessageManager;

    // location and orientation filtering
    private ExperimentProvider mExperimentProvider;
    private LocationFilter mLocationFilter;
    private OrientationFilter mOrientationFilter;

    // logfile
    private Logger mLogger;

    public LocationOrientationManager(LocationOrientationCallback callbackObject, Context context) {
        // location
        this.callbackObject = callbackObject;
        this.mContext = context;
        androidLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mMessageManager = new MessageManager();
        mLogger = new Logger();
        // setup the experiment
        setupExperiment();
        if (mLocationFilter != null) {
            mLocationFilter.logManipulationStates();
        }
        if (mOrientationFilter != null) {
            mOrientationFilter.logManipulationStates();
        }
        // start logging
        try {
            mLogger.setupLogging("exp01", mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // location listener
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Location originalLocation = new Location(location);
            if (mLocationFilter != null) {
                location = mLocationFilter.filterLocation(location);
            }
            mLogger.logLocation(originalLocation, location, mLocationFilter.getActiveManipulationList(), mOrientationFilter.getActiveManipulationList());
            if (location != null) {
                callbackObject.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * Method starts the location updates from the operating system.
     * It only attempts to listen to location updates from network and gps provider when the permission is granted by the user.
     * In addition, it tells the MessageManager object to listen for incoming messages (nearby messages forwarded from activities to eventbus).
     */
    public void startLocationUpdates() {
        this.callbackObject.onAskedForPermission();
        PackageManager mPackageManager = mContext.getPackageManager();
        int hasPerm = mPackageManager.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, mContext.getPackageName());
        // ContextCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "App has permission.");
            //this.androidLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            this.androidLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        // start to listen for messages
        mMessageManager.startListeningForMessages(this);
    }


    public void startStorageUpdates() {
        this.callbackObject.onAskedForPermission();

    }

    /**
     * Method that stops the location updates
     */
    public void stopLocationUpdates() {
        // stop listening for messages
        mMessageManager.stopListeningForMessages();
        PackageManager mPackageManager = mContext.getPackageManager();
        int hasPerm = mPackageManager.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, mContext.getPackageName());
        // ContextCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "App has permission.");
            androidLocationManager.removeUpdates(locationListener);
        }
        try {
            mLogger.stopLoggingAndWriteFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onManipulationUpdateReceived(String manipulationID, boolean manipulationState) {
        if (mLocationFilter != null) {
            mLocationFilter.updateManipulationState(manipulationID, manipulationState);
        }
        if (mOrientationFilter != null) {
            mOrientationFilter.updateManipulationState(manipulationID, manipulationState);
        }
    }

    @Override
    public void onExperimentLocationManipulationUpdatesReceived(HashMap<String, Boolean> manipulationStates) {
        // update location filter
        if (mLocationFilter != null) {
            mLocationFilter.updateManipulationStates(manipulationStates);
        }
        //updated orientation filter
        if (mOrientationFilter != null) {
            mOrientationFilter.updateManipulationStates(manipulationStates);
        }

    }

    /**
     * Setup everything for starting an experiment.
     */
    private void setupExperiment() {
        mExperimentProvider = ExperimentProvider.getInstance();
        Experiment experiment = mExperimentProvider.getExperimentWithID("exp01");
        mLocationFilter = new LocationFilter(experiment.getLocationManipulationHashMap());
        mOrientationFilter = new OrientationFilter(experiment.getOrientationManipulationHashMap());
    }

    // orientation updates
    public void startOrientationUpdates(Context applicationContext) {
        Intent sensorServiceIntent = new Intent(applicationContext, SensorService.class);
        applicationContext.startService(sensorServiceIntent);
        EventBus.getDefault().register(this);
    }

    public void stopOrientationUpdates(Context applicationContext) {
        Intent sensorServiceIntent = new Intent(applicationContext, SensorService.class);
        applicationContext.stopService(sensorServiceIntent);
        EventBus.getDefault().unregister(this);
        try {
            mLogger.stopLoggingAndWriteFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // eventbus
    @Subscribe
    public void onOrientationUpdateEvent(OrientationUpdateEvent orientationUpdateEvent) {
        // create new orientation object with timestamp
        Orientation incomingOrientation = new Orientation(orientationUpdateEvent.angleInDegrees);
        // apply filter
        Orientation filteredOrientation = mOrientationFilter.filterOrientation(incomingOrientation);
        // log
        mLogger.logOrientation(incomingOrientation, filteredOrientation, mOrientationFilter.getActiveManipulationList(), mLocationFilter.getActiveManipulationList());
        if (filteredOrientation != null) {
            callbackObject.onOrientationChanged(filteredOrientation.getAngle());
        }
    }

}
