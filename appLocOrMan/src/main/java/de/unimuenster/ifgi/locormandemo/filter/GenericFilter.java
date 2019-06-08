package de.unimuenster.ifgi.locormandemo.filter;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import de.unimuenster.ifgi.locormandemo.manipulations.Manipulation;

/**
 * Created by sven on 27.07.16.
 */
abstract class GenericFilter {

    // collections
    protected HashMap<String,Manipulation> mManipulationHashMap = new HashMap<>();

    protected ArrayList<String> mActiveManipulationList = new ArrayList<>();

    protected final String TAG = this.getClass().getSimpleName();


    /**
     * Update the state of a location manipulation
     * @param id is the ID of a location manipulation
     * @param state describes the manipulation is supposed to be active or not.
     */
    public void updateManipulationState(String id, boolean state) {
        if (mManipulationHashMap.containsKey(id)) {
            Manipulation tempMan = mManipulationHashMap.get(id);
            tempMan.setState(state);
            mManipulationHashMap.put(id,tempMan);
        }
        logManipulationStates();
        // update internal properties
        updateInternalManipulationParameters();
    }

    /**
     * Update the state of all location manipulations.
     * @param updatedHashMap HashMap with updated states
     */
    public void updateManipulationHashMap(HashMap<String, Manipulation> updatedHashMap) {
        Log.i(TAG, "updateManipulationHashMap");
        mManipulationHashMap = updatedHashMap;
        logManipulationStates();
        Log.i(TAG, "states logged");
        updateInternalManipulationParameters();
    }

    /**
     * Update every location manipulation in an experiment.
     * @param manipulationStates HashMap with manipulation ids and manipulation states.
     */
    public void updateManipulationStates(HashMap<String, Boolean> manipulationStates) {
        // clear active manipulation string list
        mActiveManipulationList.clear();
        for (String manipulationID: manipulationStates.keySet()) {
            boolean state = manipulationStates.get(manipulationID);
            if (mManipulationHashMap.containsKey(manipulationID)) {
                Manipulation tempMan = mManipulationHashMap.get(manipulationID);
                tempMan.setState(state);
                mManipulationHashMap.put(manipulationID,tempMan);
                if (state == true) {
                    mActiveManipulationList.add(manipulationID);
                }
            }
        }
        logManipulationStates();
        // update internal properties
        updateInternalManipulationParameters();
    }

    /**
     * Log the activity state of every manipulation.
     */
    public void logManipulationStates() {
        for (String key : mManipulationHashMap.keySet()) {
            Log.i(TAG, "Manipulation "+key+" is active " + mManipulationHashMap.get(key).getState());
        }
    }

    /**
     * Method that updates the internal filter parameters for manipulations based on the the currently active set of spatial and temporal manipulations.
     * Must be implemented properly by subclass.
     */
    abstract void updateInternalManipulationParameters();

    /**
     *
     * @return a list of the IDs of the active manipulations
     */
    public String[] getActiveManipulationList() {
        return mActiveManipulationList.toArray(new String[mActiveManipulationList.size()]);
    }

}
