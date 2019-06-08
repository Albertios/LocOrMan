package de.unimuenster.ifgi.locormandemo;

import java.util.HashMap;

/**
 * Created by sven on 23.06.16.
 */
public interface MessageManagerCallback {

    //void onExperimentReceived(String experimentID);

    /**
     * Update the state of a single manipulation
     * @param manipulationID
     * @param manipulationState
     */
    void onManipulationUpdateReceived(String manipulationID, boolean manipulationState);

    /**
     * Update whole set of location manipulations
     * @param  manipulationStates representation of experiment state
     */
    void onExperimentLocationManipulationUpdatesReceived(HashMap<String, Boolean> manipulationStates);
}
