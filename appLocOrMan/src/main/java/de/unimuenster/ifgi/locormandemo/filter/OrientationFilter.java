package de.unimuenster.ifgi.locormandemo.filter;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Random;

import de.unimuenster.ifgi.locormandemo.manipulations.Manipulation;
import de.unimuenster.ifgi.locormandemo.manipulations.OrientationManipulation;

/**
 * Created by sven on 28.07.16.
 */
public class OrientationFilter extends GenericFilter {
    // collection objects
    private ArrayList<Orientation> mRawOrientationHistory = new ArrayList<>();

    // coverage
    private boolean mCovManipActive = false;

    // systematic accuracy
    private boolean mSystAccManipActive = false;
    private double mSystAccuracyDegrees = 0.0;
    private double mThresholdForOrientationChange = 1.0; // one degree

    // granularity
    private boolean mGranularityManipActive = false;
    private int mNumberCardinalDirections = 0;

    // internal update rate parameters
    private boolean mUpdateRateManipActive = false;
    private double mUpdateRateSeconds = 0.0;
    private long mTimestampLastOrientUpdate = 0;

    // recency
    private boolean mRecencyManipActive = false;
    private double mRecencyDelaySeconds = 0;
    private long mTimestampRecencyManipActivated = 0;

    /**
     * Create a new orientation filter.
     * @param orManHashMap contains the orientation manipulations that are part of an experiment.
     */
    public OrientationFilter(HashMap<String,OrientationManipulation> orManHashMap){
        mManipulationHashMap.putAll(orManHashMap);
        // update internal values initially
        updateInternalManipulationParameters();
    }

    /**
     * Manipulate an incoming orientation based on the currently active manipulations of this filter instance.
     * @param incomingOrientation the new orientation measured by device.
     * @return a manipulated Orientation object, potentially null.
     */
    public Orientation filterOrientation(Orientation incomingOrientation) {
        Log.i(TAG, "Filter orientation: "+incomingOrientation.getAngle());

        // create new orientation object with timestamp
        Orientation orientation = incomingOrientation;

        // save unaltered orientation
        mRawOrientationHistory.add(orientation);

        // apply temporal filters
        orientation = applyTemporalFilter(orientation);

        if (orientation == null) {
            return null;
        }

        // apply spatial filters
        orientation = applySpatialFilter(orientation);
        if (orientation != null) {
            Log.i(TAG, "new Orientation: "+orientation.getAngle());
        }

        return orientation;
    }

    /**
     * Method that applies the temporal filter
     * @param orientation
     * @return The orientation after applying the temporal filter (potentially null);
     */
    private Orientation applyTemporalFilter(Orientation orientation) {

        // update rate
        long tempTimestampLastLocUpdate = orientation.getTimestamp();

        if (mUpdateRateManipActive) {
            //Log.i(TAG, "Apply update rate manipulation");
            // do temporal stuff here
            double deltaTime = (orientation.getTimestamp() - mTimestampLastOrientUpdate) / 1000.0;
            if (deltaTime < mUpdateRateSeconds) {
                //Log.i(TAG, "Not yet time for update");
                orientation = null;
                return orientation;
            }
        }

        // save timestamp
        mTimestampLastOrientUpdate = tempTimestampLastLocUpdate;

                /* recency
        * location update possible -> look if a delay manipulation is active -> try to find a location back in time
        * TODO: Address case when delay value is updated:
        * Right now the already elapsed delay time from the previous recency manipulation is not taken into account.
        * */
        if (mRecencyManipActive) {
            //Log.i(TAG, "Apply recency manipulation");
            long desiredTimestamp = orientation.getTimestamp() - (long)(mRecencyDelaySeconds * 1000);
            // only return old location after we waited for the delay seconds
            if (desiredTimestamp >= mTimestampRecencyManipActivated) {
                orientation = getOrientationBackInTime(desiredTimestamp, mRawOrientationHistory, 0.5);
            } else {
                orientation = null;
            }

        }

        return orientation;
    }

    /**
     * Method that applies the spatial manipulations for granularity, precision, systematic accuracy, display accuracy, coverage and incompleteness.
     * Note: Incompleteness not yet implemented.
     * @param orientation
     * @return The filtered orientation, potentially null.
     */
    private Orientation applySpatialFilter(Orientation orientation) {

        boolean changeInOrientation = checkIfOrientationHasChanged(mThresholdForOrientationChange); //

        // accuracy
        if  (changeInOrientation && mSystAccManipActive) {
            // make orientation jump
            orientation = generateRandomJump(orientation, mSystAccuracyDegrees);
        } else if (!changeInOrientation && mSystAccManipActive) {
            return null;
        }

        // granularity
        if (mGranularityManipActive) {
            orientation = applyGranularityManipulation(orientation, mNumberCardinalDirections);
        }

        // coverage
        if (mCovManipActive) {
            //Log.i(TAG, "Apply no coverage manipulation");
            return null;
        }

        return orientation;
    }

    /**
     * Get ranking of active orientation manipulations in space and time
     * @param dimension that is SPACE or TIME
     * @return
     */
    private ArrayList<String> rankActiveOrientationManipulationsWith(Manipulation.Dimension dimension) {
        ArrayList<String> rankedManipulationStrings = new ArrayList<>();
        for (String key : mManipulationHashMap.keySet()) {
            OrientationManipulation tempOrMan = (OrientationManipulation) mManipulationHashMap.get(key);
            if (tempOrMan.getState() == true && tempOrMan.getDimension() == dimension) {
                // no coverage case
                if (tempOrMan.getType() == Manipulation.Type.NO_COVERAGE) {
                    rankedManipulationStrings.clear();
                    rankedManipulationStrings.add(tempOrMan.getId());
                    return rankedManipulationStrings;
                } else {
                    // TODO: actually apply ranking here
                    rankedManipulationStrings.add(tempOrMan.getId());
                }
            }
        }
        return rankedManipulationStrings;
    }

    /**
     * Method that updates the internal filter parameters for orientation manipulations based on the the currently active set of spatial and temporal manipulations.
     */
    @Override
    void updateInternalManipulationParameters() {
        // get and rank active manipulations
        ArrayList<String> activeSpatialManipulations = rankActiveOrientationManipulationsWith(Manipulation.Dimension.SPACE);
        ArrayList<String> activeTemporalManipulations = rankActiveOrientationManipulationsWith(Manipulation.Dimension.TIME);

        // reset internal properties, ranking might have changed them completely
        resetInternalManipulationProperties();

        // spatial manipulations
        for (String idKey:activeSpatialManipulations) {
            OrientationManipulation manipulation = (OrientationManipulation) mManipulationHashMap.get(idKey);
            switch (manipulation.getType()) {
                case ACCURACY:
                    mSystAccManipActive = true;
                    mSystAccuracyDegrees = manipulation.getValue();
                    break;
                case PRECISION:
                    //mPrecisionManipActive = true;
                    //mPrecisionMeters = manipulation.getValue();
                    break;
                case GRANULARITY:
                    mGranularityManipActive = true;
                    mNumberCardinalDirections = (int) manipulation.getValue();
                    break;
                case DISPLAY_ACCURACY:
                    //mDispAccManipActive = true;
                    //mDispAccuracyMeters = manipulation.getValue();
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
            OrientationManipulation manipulation = (OrientationManipulation) mManipulationHashMap.get(idKey);
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
                    double manipulationValue = manipulation.getValue();
                    // check if recency manipulation was turned on previously or changed the temporal threshold
                    if (mRecencyManipActive == false || manipulationValue != mRecencyDelaySeconds) {
                        mTimestampRecencyManipActivated = System.currentTimeMillis();
                    }
                    mRecencyManipActive = true;
                    mRecencyDelaySeconds = manipulationValue;
                    break;
            }
        }

    }

    /**
     * Method that reset the internal manipulation parameters.
     */
    private void resetInternalManipulationProperties() {
        mCovManipActive = false;
        mSystAccManipActive = false;
        mGranularityManipActive = false;
        mUpdateRateManipActive = false;
        mRecencyManipActive = false;
    }

    /**
     * Method that tries to find a location back in time in an array list of locations if past locations are needed
     * @param timestamp The timestamp we are searching a location for.
     * @param orientationArrayList The list of past locations (beginning contains oldest locations)
     * @param timeThresholdSeconds The threshold in seconds the possible candidate for a location might deviate from the desired timestamp.
     * @return
     */
    private Orientation getOrientationBackInTime(long timestamp, ArrayList<Orientation> orientationArrayList, double timeThresholdSeconds) {
        Orientation resultOrientation = null;
        //Log.i(TAG, "timeThresholdSeconds: "+timeThresholdSeconds);
        long timeThresholdMilliseconds = (long)(timeThresholdSeconds * 1000);
        //Log.i(TAG, "timeThresholdMillisecondsSeconds: "+timeThresholdMilliseconds);
        // iterate backwards over array
        ListIterator iterator = orientationArrayList.listIterator(orientationArrayList.size());
        while (iterator.hasPrevious()) {
            //Log.i(TAG,"step back");
            Orientation tempOrientation = (Orientation)iterator.previous();
            if (tempOrientation.getTimestamp()-timeThresholdMilliseconds <= timestamp && timestamp <= tempOrientation.getTimestamp()+timeThresholdMilliseconds) {
                // location found that falls into the window
                //Log.i(TAG,"found value");
                resultOrientation = new Orientation(tempOrientation.getAngle(),tempOrientation.getTimestamp());
                break;
            }
            if (tempOrientation.getTimestamp()-timeThresholdMilliseconds < timestamp) {
                // locations are too old
                break;
            }

        }
        return resultOrientation;
    }

    /**
     * Method that generates a random orientation jump within the given range (cone shaped).
     * @param unchangedOrientation is the orientation used as the starting point for the random jump.
     * @param rangeInDegrees is the range in which the location jumps.
     * @return
     */
    private Orientation generateRandomJump(Orientation unchangedOrientation, double rangeInDegrees) {

        Log.i(TAG, "Input angle: "+unchangedOrientation.getAngle());
        // randomize direction
        boolean jumpCCW = new Random().nextBoolean();
        double direction = 1.0;
        if (jumpCCW) direction = -1.0;

        double deltaAngle = direction * rangeInDegrees*0.5 * Math.random();
        double newDirection = (unchangedOrientation.getAngle() + deltaAngle) % 360.0;
        if (newDirection < 0.0) {
            newDirection = 360.0 - newDirection;
        }
        Log.i(TAG, "Output angle: "+newDirection);
        return new Orientation(newDirection, unchangedOrientation.getTimestamp());
    }

    /**
     * Method that applies the granularity manipulation.
     * @param unchangedOrientation input orientation
     * @param numberCardinalDirections as int: Has to be divisible by 4
     * @return The orientation mapped to one of the cardinal directions
     */
    private Orientation applyGranularityManipulation(Orientation unchangedOrientation, int numberCardinalDirections) {

        double input = unchangedOrientation.getAngle();
        Log.i(TAG, "Input orientation: "+unchangedOrientation.getAngle());
        double result = input;

        if (numberCardinalDirections % 4 != 0) {
            // throw illegal argument exception?
            return unchangedOrientation;
        }

        double coneWidth = 360.0 / numberCardinalDirections;

        // define starting point
        double previousBound =  -coneWidth / 2.0;
        for (int i=1; i<=numberCardinalDirections; i++) {
            double bound = previousBound + coneWidth;
            if ( (result > previousBound && result <= bound) || (previousBound < 0 && (result - 360.0) > previousBound) ) {
                result = bound - coneWidth / 2.0;
                break;
            }
            previousBound += coneWidth;
        }

        return new Orientation(result, unchangedOrientation.getTimestamp());
    }


    private boolean checkIfOrientationHasChanged(double minAngularDelta) {
        boolean orientationHasChanged = false;
        if (mRawOrientationHistory.size() < 2) {
            return true;
        }
        // get last location (current location is last entry)
        if (mRawOrientationHistory.get(mRawOrientationHistory.size()-2) != null) {
            Orientation currentOrientation = mRawOrientationHistory.get(mRawOrientationHistory.size()-2);
            Orientation previousOrientation = mRawOrientationHistory.get(mRawOrientationHistory.size()-1);
            // compute delta
            double delta = computeDelta(currentOrientation.getAngle(),previousOrientation.getAngle());
            if (delta >= minAngularDelta) {
                orientationHasChanged = true;
                Log.i(TAG, "Change in orientation");
            } else {
                Log.i(TAG, "No change in orientation");
            }
        }
        return orientationHasChanged;
    }

    /**
     * Compute the distance between two angles
     * Based on: http://stackoverflow.com/a/7571008
     * @param sourceAngle first angle
     * @param targetAngle second angle
     * @return distance between those angles in degrees
     */
    private double computeDelta(double sourceAngle, double targetAngle) {
        double phi = Math.abs(targetAngle - sourceAngle) % 360;       // This is either the distance or 360 - distance
        double distance = phi > 180 ? 360 - phi : phi;
        return distance;
    }

}
