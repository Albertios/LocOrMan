package de.unimuenster.ifgi.locormandemo.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class MockLocationService extends Service {

    private final String TAG = MockLocationService.class.getSimpleName();

    private LocationManager mAndroidLocationManager;
    private LocationListener mLocationListener;
    final String mProviderName = "MyGpsProvider";
    boolean accuracyFlag = true;
    Location previousLocation = null;
    int mCounter = 0;

    public MockLocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAndroidLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // location listener
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                /*
                if (previousLocation != null && previousLocation.getElapsedRealtimeNanos() == location.getElapsedRealtimeNanos()) {
                    Log.i(TAG, "same location");
                }
                */
                Log.i(TAG,"location update");
                if (previousLocation != null && previousLocation.getElapsedRealtimeNanos() == location.getElapsedRealtimeNanos()) { // location.isFromMockProvider does not work
                    Log.i(TAG,"location is mocked");
                    previousLocation = location;
                    return;
                } else {
                    Location originalLocation = new Location(location);
                    Log.i(TAG,"location in background from provider: "+location.getProvider());

                    Location loc = new Location(LocationManager.GPS_PROVIDER);
                    /*
                    if (accuracyFlag) {
                        loc.setAccuracy(0);
                    }
                    else {
                        loc.setAccuracy(30);
                    }
                    accuracyFlag = !accuracyFlag;
                    */
                    loc.setAccuracy(location.getAccuracy());
                    loc.setAltitude(location.getAltitude());
                    loc.setBearing(location.getBearing());
                    loc.setLatitude(location.getLatitude()+mCounter);
                    loc.setLongitude(location.getLongitude());
                    loc.setSpeed(location.getSpeed());
                    loc.setTime(location.getTime());
                    loc.setElapsedRealtimeNanos(location.getElapsedRealtimeNanos());

                    mAndroidLocationManager.setTestProviderLocation(mProviderName, loc);
                    mCounter += 1;
                }
                previousLocation = location;
                /*
                if (mLocationFilter != null) {
                    location = mLocationFilter.filterLocation(location);
                }
                mLogger.logLocation(originalLocation, location, mLocationFilter.getActiveManipulationList(), mOrientationFilter.getActiveManipulationList());
                if (location != null) {
                    callbackObject.onLocationChanged(location);
                }
                */
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

        setTestProvider(mProviderName);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        };

        mAndroidLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        mAndroidLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        mAndroidLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void setTestProvider(String providerName) {
        //mAndroidLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        if (mAndroidLocationManager != null && mAndroidLocationManager.getProvider(providerName) != null) {
            mAndroidLocationManager.removeTestProvider(providerName);
        }
        mAndroidLocationManager.addTestProvider(providerName,true,true,true,false,false,true,true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
        mAndroidLocationManager.setTestProviderEnabled(providerName, true);
        //mAndroidLocationManager.setTestProviderStatus();
    }

    private void removeTestProvider(String providerName) {
        //mAndroidLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        if (mAndroidLocationManager != null && mAndroidLocationManager.getProvider(providerName) != null) {
            mAndroidLocationManager.removeTestProvider(providerName);
        }
    }

}
