package de.unimuenster.ifgi.locormandemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVWriter;
import de.unimuenster.ifgi.locormandemo.filter.Orientation;
import de.unimuenster.ifgi.locormandemo.manipulations.ExperimentProvider;

/**
 * Created by sven on 02.08.16.
 */
public class Logger {

    private String mLogfileName = "default";
    private boolean mAppendToLogfile;

    private String mFilePath = "";
    private File mFile;
    private CSVWriter mWriter;

    private String mExperimentID = "default";
    private String[] fileHeaderOriginalData = {"ID", "log_timestamp","location_timestamp", "lat", "lon", "accuracy", "bearing","orientation"};
    private String[] fileHeaderManipulatedData = {"ID", "log_timestamp","location_timestamp", "lat", "lon", "accuracy", "bearing","orientation","active_manipulations"};
    private String[] fileHeaderCombined = {"ID","event","log_timestamp","original_location_timestamp", "original_lat", "original_lon", "original_accuracy","original_orientation_timestamp","original_orientation","manipulated_location_timestamp", "manipulated_lat", "manipulated_lon", "manipulated_accuracy","manipulated_orientation_timestamp","manipulated_orientation", "active_location_manipulations", "active_orientation_manipulations", "orManipSystAcc01Value", "dispAccManipValue", "systAccManipVaule", "recencyManip02Value", "orManipRecency01Value"};

    private long mID = 0;

    private Orientation mLastKnownOriginalOrientation;
    private Orientation mLastKnownManipulatedOrientation;
    private Location mLastKnownOriginalLocation;
    private Location mLastKnownManipulatedLocation;

    Context mContext;

    public void setupLogging(String experimentID, Context context) throws IOException {

        mContext = context;

        // load logging settings from shared prefs
        loadLogfileSettingsFromSharedPrefs();

        mExperimentID = experimentID;
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath = baseDir + File.separator + mLogfileName;

        mFile = new File(mFilePath);

        // file is already there
        if (mFile.exists() && !mFile.isDirectory() && mAppendToLogfile) {
            mWriter = new CSVWriter(new FileWriter(mFilePath,true));
        // file needs to be created or I don't want appending of a file: create new file
        } else {
            mWriter = new CSVWriter(new FileWriter(mFilePath));
            mWriter.writeNext(fileHeaderCombined);
        }
    }

    private void loadLogfileSettingsFromSharedPrefs() {
        SharedPreferences sharedPrefs = mContext.getSharedPreferences(GlobalConstants.SHARED_PREFS_NAME,Context.MODE_PRIVATE);
        mLogfileName = sharedPrefs.getString(GlobalConstants.SHARED_PREFS_LOGFILE_NAME_KEY,GlobalConstants.DEFAULT_LOGFILE_NAME);
        mAppendToLogfile = sharedPrefs.getBoolean(GlobalConstants.SHARED_PREFS_LOGFILE_APPEND_KEY, true);
    }

    public void logLocation(Location originalLocation, Location manipulatedLocation, String[] activeLocationManipulations, String[] activeOrientationManipulations) {
        mLastKnownOriginalLocation = originalLocation;
        mLastKnownManipulatedLocation = manipulatedLocation;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String idString = ""+mID;
        String event = "location update";
        String logTimestamp = dateFormat.format(System.currentTimeMillis());
        String originalLocationTimestamp = dateFormat.format(mLastKnownOriginalLocation.getTime());
        String originalLat = ""+mLastKnownOriginalLocation.getLatitude();
        String originalLon = ""+mLastKnownOriginalLocation.getLongitude();
        String originalAccuracy = ""+mLastKnownOriginalLocation.getAccuracy();
        String originalOrientationTimestamp = "-";
        String originalOrientationAngle = "-";
        if (mLastKnownOriginalOrientation != null) {
            originalOrientationTimestamp = dateFormat.format(mLastKnownOriginalOrientation.getTimestamp());
            originalOrientationAngle = ""+mLastKnownOriginalOrientation.getAngle();
        }
        String manipulatedLocationTimestamp = "-";
        String manipulatedLat = "-";
        String manipulatedLon = "-";
        String manipulatedAccuracy = "-";
        if (mLastKnownManipulatedLocation != null) {
            manipulatedLocationTimestamp = dateFormat.format(mLastKnownManipulatedLocation.getTime());
            manipulatedLat = ""+mLastKnownManipulatedLocation.getLatitude();
            manipulatedLon = ""+mLastKnownManipulatedLocation.getLatitude();
            manipulatedAccuracy = ""+mLastKnownManipulatedLocation.getAccuracy();
        }
        String manipulatedOrientationTimestamp = "-";
        String manipulatedOrientationAngle = "-";
        if (mLastKnownManipulatedOrientation != null) {
            manipulatedOrientationTimestamp = dateFormat.format(mLastKnownManipulatedOrientation.getTimestamp());
            manipulatedOrientationAngle = ""+mLastKnownManipulatedOrientation.getAngle();
        }

        String locationManipulations = "[]";
        String orientationManipulations = "[]";
        if (activeLocationManipulations != null) {
            locationManipulations = Arrays.toString(activeLocationManipulations);
        }
        if (activeOrientationManipulations != null) {
            orientationManipulations = Arrays.toString(activeOrientationManipulations);
        }

        String[] data = {idString, event, logTimestamp, originalLocationTimestamp, originalLat, originalLon, originalAccuracy, originalOrientationTimestamp, originalOrientationAngle,
        manipulatedLocationTimestamp, manipulatedLat, manipulatedLon, manipulatedAccuracy, manipulatedOrientationTimestamp, manipulatedOrientationAngle, locationManipulations, orientationManipulations};

        mWriter.writeNext(data);

        mID += 1;
    }

    public void logOrientation(Orientation originalOrientation, Orientation manipulatedOrientation,  String[] activeOrientationManipulations, String[] activeLocationManipulations) {
        mLastKnownOriginalOrientation = originalOrientation;
        mLastKnownManipulatedOrientation = manipulatedOrientation;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String idString = ""+mID;
        String event = "orientation update";
        String logTimestamp = dateFormat.format(System.currentTimeMillis());

        String originalLocationTimestamp = "-";
        String originalLat = "-";
        String originalLon = "-";
        String originalAccuracy = "-";
        if (mLastKnownOriginalLocation != null) {
            originalLocationTimestamp = dateFormat.format(mLastKnownOriginalLocation.getTime());
            originalLat = ""+mLastKnownOriginalLocation.getLatitude();
            originalLon = ""+mLastKnownOriginalLocation.getLongitude();
            originalAccuracy = ""+mLastKnownOriginalLocation.getAccuracy();
        }

        String originalOrientationTimestamp = "-";
        String originalOrientationAngle = "-";
        if (mLastKnownOriginalOrientation != null) {
            originalOrientationTimestamp = dateFormat.format(mLastKnownOriginalOrientation.getTimestamp());
            originalOrientationAngle = ""+mLastKnownOriginalOrientation.getAngle();
        }

        String manipulatedLocationTimestamp = "-";
        String manipulatedLat = "-";
        String manipulatedLon = "-";
        String manipulatedAccuracy = "-";
        if (mLastKnownManipulatedLocation != null) {
            manipulatedLocationTimestamp = dateFormat.format(mLastKnownManipulatedLocation.getTime());
            manipulatedLat = ""+mLastKnownManipulatedLocation.getLatitude();
            manipulatedLon = ""+mLastKnownManipulatedLocation.getLatitude();
            manipulatedAccuracy = ""+mLastKnownManipulatedLocation.getAccuracy();
        }

        String manipulatedOrientationTimestamp = "-";
        String manipulatedOrientationAngle = "-";
        if (mLastKnownManipulatedOrientation != null) {
            manipulatedOrientationTimestamp = dateFormat.format(mLastKnownManipulatedOrientation.getTimestamp());
            manipulatedOrientationAngle = ""+mLastKnownManipulatedOrientation.getAngle();
        }

        String locationManipulations = "[]";
        String orientationManipulations = "[]";
        if (activeLocationManipulations != null) {
            locationManipulations = Arrays.toString(activeLocationManipulations);
        }
        if (activeOrientationManipulations != null) {
            orientationManipulations = Arrays.toString(activeOrientationManipulations);
        }

        String orManipSystAcc01Value = "" + ExperimentProvider.MyClass.orManipSystAcc01Value;
        String dispAccManipValue = "" + ExperimentProvider.MyClass.dispAccManipValue;
        String systAccManipVaule = "" + ExperimentProvider.MyClass.systAccManipVaule;
        String recencyManip02Value= "" + ExperimentProvider.MyClass.recencyManip02Value;
        String orManipRecency01Value= "" + ExperimentProvider.MyClass.orManipRecency01Value;
        

        String[] data = {idString, event, logTimestamp, originalLocationTimestamp, originalLat, originalLon, originalAccuracy, originalOrientationTimestamp, originalOrientationAngle,
                manipulatedLocationTimestamp, manipulatedLat, manipulatedLon, manipulatedAccuracy, manipulatedOrientationTimestamp, manipulatedOrientationAngle, locationManipulations, orientationManipulations, orManipSystAcc01Value, dispAccManipValue, systAccManipVaule, recencyManip02Value, orManipRecency01Value};

        mWriter.writeNext(data);

        mID +=1;
    }

    public void stopLoggingAndWriteFile() throws IOException {

       mWriter.flush();
       mWriter.close();

        // trigger indexing of files (needed for some devices to appear on the sdcard)
        Uri contentUri = Uri.fromFile(mFile);
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE"); mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);

    }
}
