package de.unimuenster.ifgi.locormandemo;

import android.location.Location;

/**
 * Created by sven on 17.06.16.
 */
public interface LocationOrientationCallback {

    void onLocationChanged(Location newLocation);
    void onOrientationChanged(double newOrientation);
    void onAskedForPermission();


}
