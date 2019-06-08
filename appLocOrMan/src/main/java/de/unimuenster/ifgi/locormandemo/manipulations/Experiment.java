package de.unimuenster.ifgi.locormandemo.manipulations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unimuenster.ifgi.locormandemo.JsonUtility;

/**
 * Created by sven on 23.06.16.
 */
public class Experiment {
    private String id;
    private String name;
    private HashMap<String,LocationManipulation> locationManipulationHashMap = new HashMap<String,LocationManipulation>();
    private HashMap<String,OrientationManipulation> orientationManipulationHashMap = new HashMap<String,OrientationManipulation>();



    public Experiment(String id) {
        this(id, "");
    }

    public Experiment(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, LocationManipulation> getLocationManipulationHashMap() {
        return locationManipulationHashMap;
    }

    public void setLocationManipulationHashMap(HashMap<String, LocationManipulation> locationManipulationHashMap) {
        this.locationManipulationHashMap = locationManipulationHashMap;
    }

    public HashMap<String, OrientationManipulation> getOrientationManipulationHashMap() {
        return orientationManipulationHashMap;
    }

    public void setOrientationManipulationHashMap(HashMap<String, OrientationManipulation> orientationManipulationHashMap) {
        this.orientationManipulationHashMap = orientationManipulationHashMap;
    }

    public void addLocationManipulation(LocationManipulation locMan) {
        this.locationManipulationHashMap.put(locMan.id, locMan);
    }

    public void addOrientationManipulation(OrientationManipulation oriMan) {
        this.orientationManipulationHashMap.put(oriMan.id, oriMan);
    }

    /**
     * Get a list of all the location and orientation manipulations that are part of the experiment.
     * @return List of Manipulation objects.
     */
    public List<Manipulation> getManipulationList() {
        /*
        List<Manipulation> resultList = this.locationManipulationHashMap.entrySet().stream()
                .map(e -> Map.Entry::getValue)
                .collect(Collectors.toList());
                */
        ArrayList<Manipulation> resultList = new ArrayList<>();


        for (Map.Entry<String,LocationManipulation> entry : locationManipulationHashMap.entrySet()) {
            resultList.add(entry.getValue());
        }
        for (Map.Entry<String,OrientationManipulation> entry : orientationManipulationHashMap.entrySet()) {
            resultList.add(entry.getValue());
        }

        return resultList;
    }

    /**
     * Method to set the state of a manipulation that is part of this experiment.
     * @param manipulationID
     * @param state
     */
    public void setStateOfManipulation(String manipulationID, boolean state) {
        if (locationManipulationHashMap.containsKey(manipulationID)) {
            LocationManipulation tempLocMan = locationManipulationHashMap.get(manipulationID);
            tempLocMan.setState(state);
            locationManipulationHashMap.put(manipulationID,tempLocMan);
        } else if (orientationManipulationHashMap.containsKey(manipulationID)) {
            OrientationManipulation tempOrMan = orientationManipulationHashMap.get(manipulationID);
            tempOrMan.setState(state);
            orientationManipulationHashMap.put(manipulationID,tempOrMan);
        }
    }

    /**
     * Method that returns the manipulation states as a json message
     * @return jsone message representing the current state of the manipulations used in the experiment
     */
    public String getManipulationsStateJSONString() {
        return JsonUtility.generateExperimentUpdateJsonMessage(id, locationManipulationHashMap, orientationManipulationHashMap);
    }



}
