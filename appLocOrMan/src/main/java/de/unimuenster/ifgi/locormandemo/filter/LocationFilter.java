package de.unimuenster.ifgi.locormandemo.filter;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Random;

import de.unimuenster.ifgi.locormandemo.manipulations.LocationManipulation;
import de.unimuenster.ifgi.locormandemo.manipulations.Manipulation;

/**
 * Created by sven on 29.06.16.
 */
public class LocationFilter extends GenericFilter {

    // collection objects
    private ArrayList<Location> mRawLocationHistory = new ArrayList<>();
    private ArrayList<Location> mManipulatedLocationHistory = new ArrayList<>();

    // display accuracy
    // app: location: display accuracy (30m)
    private boolean mDispAccManipActive = false;
    private double mDispAccuracyMeters = 0.0;
    private int locationDisplayAccuracy; // todo albert variable für die numberID

    // coverage
    // app: 5. orientation: no coverage
    private boolean mCovManipActive = false;

    // systematic accuracy
    // app: 4. location: low accuracy (20m)
    private boolean mSystAccManipActive = false;
    private double mSystAccuracyMeters = 0.0;
    private double mThresholdForLocationChange = 2.0; // meters
    private Location mPrevManipulatedAccuracyLocation = null;
    private Location mLastOriginalAccuracyLocation = null;
    private boolean mSystAccManipNotYetAppliedButSwitchedOn = false;

    // granularity
    private boolean mGranularityManipActive = false;
    private double mGranularityMeters = 0.0;

    // precision
    private boolean mPrecisionManipActive = false;
    private double mPrecisionMeters = 0.0;

    // internal update rate parameters
    private boolean mUpdateRateManipActive = false;
    private double mUpdateRateSeconds = 0.0;
    private long mTimestampLastLocUpdate = 0;

    // recency
    private boolean mRecencyManipActive = false;
    private int mRecencyDelaySeconds = 0;
    private long mTimestampRecencyManipActivated = 0;
    private boolean mRecencyDelayDidChange = false;

    /**
     * Create a new location filter.
     * @param locManHashMap contains the location manipulations that are part of an experiment.
     */
    public LocationFilter(HashMap<String,LocationManipulation> locManHashMap){
        mManipulationHashMap.putAll(locManHashMap);
        // update internal values initially
        updateInternalManipulationParameters();
    }

    /**
     * Manipulate an incoming location based on the currently active manipulations of this filter instance.
     * @param incomingLocation the new location coming from the operating system.
     * @return a manipulated Location object, potentionally null.
     */




    // hier geht es los -----------------------------------------------------------------------------------------------
    public Location filterLocation(Location incomingLocation) {
        Log.i(TAG, "Filter location");
        // apply filter based on active manipulations

        // save unaltered location
        mRawLocationHistory.add(new Location(incomingLocation));

        // apply temporal filters
        incomingLocation = applyTemporalFilter(incomingLocation);

        if (incomingLocation == null) {
            return null;
        }

        // apply spatial filters
        incomingLocation = applySpatialFilter(incomingLocation);

        return incomingLocation;
    }

    /**
     * Method that applies the temporal filter
     * @param location
     * @return The location after applying the temporal filter (potentionally null);
     */
    private Location applyTemporalFilter(Location location) {
        //Log.i(TAG, "Apply temporal location filter:");

        // update rate
        long tempTimestampLastLocUpdate = location.getTime();

        if (mUpdateRateManipActive) {
            //Log.i(TAG, "Apply update rate manipulation");
            // do temporal stuff here
            double deltaTime = (location.getTime() - mTimestampLastLocUpdate) / 1000.0;
            if (deltaTime < mUpdateRateSeconds) {
                //Log.i(TAG, "Not yet time for update");
                location = null;
                return location;
            }
        }

        // save timestamp
        mTimestampLastLocUpdate = tempTimestampLastLocUpdate;

        /* recency
        * location update possible -> look if a delay manipulation is active -> try to find a location back in time
        * TODO: Address case when delay value is updated:
        * Right now the already elapsed delay time from the previous recency manipulation is not taken into account.
        * */
        if (mRecencyManipActive) {
            //Log.i(TAG, "Apply recency manipulation");
            long desiredTimestamp = location.getTime() - mRecencyDelaySeconds * 1000;
            // only return old location after we waited for the delay seconds
            if (desiredTimestamp >= mTimestampRecencyManipActivated) {
                location = getLocationBackInTime(desiredTimestamp, mRawLocationHistory, 2);
            } else {
                location = null;
            }

        }

        return location;
    }

    /**
     * Method that applies the spatial manipulations for granularity, precision, systematic accuracy, display accuracy, coverage and incompleteness.
     * Note: Incompleteness not yet implemented.
     * @param location
     * @return The filtered location, potentially null.
     */
    private Location applySpatialFilter(Location location) {
        //Log.i(TAG, "Apply spatial location filter");

        // Determine if there was a change in the location TODO: implement properly
        boolean changeInRawLocation = checkIfRawLocationHasChanged(mThresholdForLocationChange);
        boolean changeInLocation = changeInRawLocation || mSystAccManipNotYetAppliedButSwitchedOn; // this might miss the case when a number of small location updates where received
        if (mLastOriginalAccuracyLocation != null) {
            // check distance between last location that was taken for accuracy manipulation and
            boolean distanceLargeEnough = checkIfLocationsAreSufficientlyApartForLocationChange(mThresholdForLocationChange, mLastOriginalAccuracyLocation, location);
            changeInLocation = changeInLocation || distanceLargeEnough;
        }

        // accuracy that might be changed in the course of the manipulation
        double localDisplayAccuracy = location.getAccuracy();

        // apply granularity
        if (mGranularityManipActive && location.getAccuracy() < mGranularityMeters) {
            localDisplayAccuracy = mGranularityMeters;
        }

        if (changeInLocation && mSystAccManipActive) {
            //Log.i(TAG, "Apply systematic accuracy manipulation");
            // store raw location at time of applicaiton of systematic accuracy manipulation
            mLastOriginalAccuracyLocation = new Location(location);
            // apply systematic accuracy and generate a random jump
            // make location jump
            location = generateRandomJump(location, mSystAccuracyMeters);
            // update accuracy
            localDisplayAccuracy = computeRandomValueForDisplayAccuracy(mSystAccuracyMeters, mSystAccuracyMeters * 0.3);
        } else if (!changeInLocation && mSystAccManipActive) {
            // no change in location detected but systematic accuracy manipulation active: Location will be updated (only small jump) and accuracy as well?
            localDisplayAccuracy = computeRandomValueForDisplayAccuracy(mSystAccuracyMeters, mSystAccuracyMeters * 0.3);
            // set to previously manipulated location
            if (mPrevManipulatedAccuracyLocation != null) {
                location.set(mPrevManipulatedAccuracyLocation);
                // one could apply a position offset here as well to simulate original precision?
            }
        } else if (!changeInLocation && mPrecisionManipActive) {
            //Log.i(TAG, "Apply precision manipulation");
            // apply precision manipulation
        }

        // apply display accuracy, hard: not as lower bound
        if (mDispAccManipActive) {
            //Log.i(TAG, "Apply display accuracy manipulation");
            // add random variation of max. 30 percent of the display accuracy
            localDisplayAccuracy = computeRandomValueForDisplayAccuracy(mDispAccuracyMeters, mDispAccuracyMeters * 0.3);
        }

        // set accuracy:
        location.setAccuracy((float)localDisplayAccuracy);

        // store outcome of accuracy manipulation
        if (mSystAccManipActive) {
            mPrevManipulatedAccuracyLocation = new Location(location);
            mSystAccManipNotYetAppliedButSwitchedOn = false; // turn of first time switch again
        } else {
            // reset temporal location objects
            mPrevManipulatedAccuracyLocation = null;
            mLastOriginalAccuracyLocation = null;
        }

        // coverage
        if (mCovManipActive) {
            //Log.i(TAG, "Apply no coverage manipulation");
            return null;
        }

        // incompleteness: not yet implemented

        return location;
    }

    /**
     * Get ranking of active location manipulations in space and time
     * @param dimension that is SPACE or TIME
     * @return ArrayList of Strings that contain the ranked manipulations
     */
    private ArrayList<String> rankActiveLocationManipulationsWith(Manipulation.Dimension dimension) {
        ArrayList<String> rankedManipulationStrings = new ArrayList<>();
        for (String key : mManipulationHashMap.keySet()) {
            LocationManipulation tempLocMan = (LocationManipulation) mManipulationHashMap.get(key);
            if (tempLocMan.getState() == true && tempLocMan.getDimension() == dimension) {
                // no coverage case
                if (tempLocMan.getType() == Manipulation.Type.NO_COVERAGE) {
                    rankedManipulationStrings.clear();
                    rankedManipulationStrings.add(tempLocMan.getId());
                    return rankedManipulationStrings;
                } else {
                    // TODO: actually apply ranking here
                    rankedManipulationStrings.add(tempLocMan.getId());
                }
            }
        }
        return rankedManipulationStrings;
    }

    /**
     * Method that updates the internal filter parameters for location manipulations based on the the currently active set of spatial and temporal manipulations.
     */
    @Override
    void updateInternalManipulationParameters() {

        // get and rank active manipulations
        ArrayList<String> activeSpatialManipulations = rankActiveLocationManipulationsWith(Manipulation.Dimension.SPACE);
        ArrayList<String> activeTemporalManipulations = rankActiveLocationManipulationsWith(Manipulation.Dimension.TIME);

        // reset internal properties, ranking might have changed them completely
        resetInternalManipulationProperties();

        // spatial manipulations
        for (String idKey:activeSpatialManipulations) {
            LocationManipulation manipulation = (LocationManipulation) mManipulationHashMap.get(idKey);
            switch (manipulation.getType()) {
                case ACCURACY:
                    mSystAccManipActive = true;
                    mSystAccuracyMeters = manipulation.getValue();
                    mSystAccManipNotYetAppliedButSwitchedOn = true;
                    break;
                case PRECISION:
                    mPrecisionManipActive = true;
                    mPrecisionMeters = manipulation.getValue();
                    break;
                case GRANULARITY:
                    mGranularityManipActive = true;
                    mGranularityMeters = manipulation.getValue();
                    break;
                case DISPLAY_ACCURACY:
                    mDispAccManipActive = true;
                    mDispAccuracyMeters = manipulation.getValue();
                    break;
                case NO_COVERAGE:
                    mCovManipActive = true;
                    break;
                case UPDATE_RATE:
                    break;
                case RECENCY:
                    break;
            }
        }

        // temporal manipulations
        for (String idKey:activeTemporalManipulations) {
            LocationManipulation manipulation = (LocationManipulation) mManipulationHashMap.get(idKey);
            switch (manipulation.getType()) {
                case ACCURACY:
                    break;
                case PRECISION:
                    break;
                case GRANULARITY:
                    break;
                case DISPLAY_ACCURACY:
                    break;
                case NO_COVERAGE:
                    break;
                case UPDATE_RATE:
                    mUpdateRateManipActive = true;
                    mUpdateRateSeconds = manipulation.getValue();
                    break;
                case RECENCY:
                    int manipulationValue = (int) manipulation.getValue();
                    // check if recency manipulation was turned on previously or changed the temporal threshold
                    if (mRecencyManipActive == false || manipulationValue != mRecencyDelaySeconds) {
                        mTimestampRecencyManipActivated = System.currentTimeMillis();
                        mRecencyDelayDidChange = true;
                    }
                    mRecencyManipActive = true;
                    // TODO: rethink seconds casting
                    mRecencyDelaySeconds = manipulationValue;
                    break;
            }
        }

    }

    /**
     * Method that reset the internal manipulation parameters.
     */
    private void resetInternalManipulationProperties() {
        mDispAccManipActive = false;
        mSystAccManipActive = false;
        mCovManipActive = false;
        mGranularityManipActive = false;
        mPrecisionManipActive = false;
        mUpdateRateManipActive = false;
        mRecencyManipActive = false;
    }

    /**
     * Method that tries to find a location back in time in an array list of locations if past locations are needed
     * @param timestamp The timestamp we are searching a location for.
     * @param locationArrayList The list of past locations (beginning contains oldest locations)
     * @param timeThresholdSeconds The threshold in seconds the possible candidate for a location might deviate from the desired timestamp.
     * @return Location back in time, potentially null
     */
    private Location getLocationBackInTime(long timestamp, ArrayList<Location> locationArrayList, int timeThresholdSeconds) {
        Location resultLocation = null;
        long timeThresholdMilliseconds = timeThresholdSeconds * 1000;
        // iterate backwards over array
        ListIterator iterator = locationArrayList.listIterator(locationArrayList.size());
        while (iterator.hasPrevious()) {
            Location tempLocation = (Location)iterator.previous();
            if (tempLocation.getTime()-timeThresholdMilliseconds <= timestamp && timestamp <= tempLocation.getTime()+timeThresholdMilliseconds) {
                // location found that falls into the window
                resultLocation = new Location(tempLocation);
                break;
            }
            if (tempLocation.getTime()-timeThresholdMilliseconds < timestamp) {
                // locations are too old
                break;
            }

        }
        return resultLocation;
    }

    /**
     * Method that generates a random location jump within the given range.
     * Computation based on this http://stackoverflow.com/questions/1125144/how-do-i-find-the-lat-long-that-is-x-km-north-of-a-given-lat-long/1125425
     * Namely this post: http://stackoverflow.com/a/1125425
     * @param unchangedLocation is the location used as the starting point for the random jump.
     * @param rangeInMeters is the radius in which the location jumps.
     * @return Location after the random jump.
     */
    private Location generateRandomJump(Location unchangedLocation, double rangeInMeters) {
        Location tempLocation = new Location(unchangedLocation);

        double earthRadiusInMeters = 6378137.0;

        // generate random bearing
        double randomBearing = Math.random()*360.0;
        double randomRange = rangeInMeters*Math.random();

        double latIn = Math.toRadians(tempLocation.getLatitude());
        double lonIn = Math.toRadians(tempLocation.getLongitude());
        double angularDistance = randomRange / earthRadiusInMeters;
        double trueCourse = Math.toRadians(randomBearing);

        double latOut = Math.asin(
                Math.sin(latIn) * Math.cos(angularDistance) +
                        Math.cos(latIn) * Math.sin(angularDistance) * Math.cos(trueCourse));

        double dLonOut = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latIn),
                Math.cos(angularDistance) - Math.sin(latIn) * Math.sin(latOut));

        double lonOut = ((lonIn + dLonOut + Math.PI) % (2*Math.PI)) - Math.PI;

        tempLocation.setLatitude(Math.toDegrees(latOut));
        tempLocation.setLongitude(Math.toDegrees(lonOut));

        return tempLocation;
    }

    /**
     * Method that computes a rand variation of the location accuracy.
     * @param displayAccuracy the accuracy to be manipulated (in meters)
     * @param maxRandomDeviation the maximum of the random deviation in one direction from the given accuracy value (in meters.)
     * @return the manipulated accuracy.
     */
    private double computeRandomValueForDisplayAccuracy(double displayAccuracy, double maxRandomDeviation) {
        double result = displayAccuracy;

        // randomize deviation
        double randomDeviation = maxRandomDeviation * Math.random();

        // randomize sign of deviation
        boolean isSignPositive = new Random().nextBoolean();
        double sign = 1.0;
        if (!isSignPositive) sign = -1.0;
        result = result + sign * randomDeviation;

        return result;
    }

    /**
     * Determine if a location change did occur.
     * @param minMovementDistanceInMeters minimum distance for location change
     * @return true if the distance between the current and the previous location has been equal / larger than {@param minMovementDistanceInMeters}.
     */
    private boolean checkIfRawLocationHasChanged(double minMovementDistanceInMeters) {
        boolean locationHasChanged = false;
        if (mRawLocationHistory.size() < 2) {
            return true;
        }
        // get last location (current location is last entry)
        if (mRawLocationHistory.get(mRawLocationHistory.size()-1) != null) {
            Location currentLocation = mRawLocationHistory.get(mRawLocationHistory.size()-1);
            Location previousLocation = mRawLocationHistory.get(mRawLocationHistory.size()-2);
            // compute distance
            double distance = previousLocation.distanceTo(currentLocation);
            if (distance >= minMovementDistanceInMeters) {
                locationHasChanged = true;
            }
        }

        return locationHasChanged;
    }

    /**
     * Check if distance between two locations exceeded certain threshold.
     * @param minDistance
     * @param oldLocation
     * @param newLocation
     * @return
     */
    private boolean checkIfLocationsAreSufficientlyApartForLocationChange(double minDistance, Location oldLocation, Location newLocation) {
        double distance  = oldLocation.distanceTo(newLocation);
        if (distance >= minDistance) {
            return true;
        } else {
            return false;
        }
    }
}
