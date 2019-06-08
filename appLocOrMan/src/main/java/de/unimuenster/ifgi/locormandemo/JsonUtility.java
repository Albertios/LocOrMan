package de.unimuenster.ifgi.locormandemo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.unimuenster.ifgi.locormandemo.manipulations.LocationManipulation;
import de.unimuenster.ifgi.locormandemo.manipulations.OrientationManipulation;

/**
 * Created by sven on 06.09.16.
 */

interface JsonUtilityExperimentCallback {
    void experimentUpdateMessageParsed(HashMap<String,Boolean> manipulationStates);
    void experimentUpdateResponseParsed();
}

interface JsonUtilityLoggingCallback {
    void startLoggingMessageParsed();
    void startLoggingResponseParsed();
    void stopLoggingMessageParsed();
    void stopLoggingResponseParsed();
}

public class JsonUtility {

    private static final String TYPE_EXPERIMENT_UPDATE = "experimentUpdate";
    private static final String TYPE_EXPERIMENT_UPDATE_RESPONSE = "experimentUpdateResponse";
    private static final String TYPE_LOGGING_START = "loggingStart";
    private static final String TYPE_LOGGING_START_RESPONSE = "loggingStartResponse";
    private static final String TYPE_LOGGING_STOP = "loggingStop";
    private static final String TYPE_LOGGING_STOP_RESPONSE = "loggingStopResponse";

    private static final String KEY_MESSAGE_TYPE = "type";

    private JsonUtilityExperimentCallback mExperimentCallback;
    private JsonUtilityLoggingCallback mLoggingCallback;

    public JsonUtility(JsonUtilityExperimentCallback jsonUtilityExperimentCallback) {
        mExperimentCallback = jsonUtilityExperimentCallback;
    }

    public JsonUtility(JsonUtilityLoggingCallback jsonUtilityLoggingCallback) {
        mLoggingCallback = jsonUtilityLoggingCallback;
    }

    public JsonUtility(JsonUtilityExperimentCallback jsonUtilityExperimentCallback, JsonUtilityLoggingCallback jsonUtilityLoggingCallback) {
        mExperimentCallback = jsonUtilityExperimentCallback;
        mLoggingCallback = jsonUtilityLoggingCallback;
    }

    public void parseMessage(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String messageType = jsonObject.getString(KEY_MESSAGE_TYPE);
            switch (messageType) {
                case TYPE_EXPERIMENT_UPDATE:
                    parseExperimentUpdateMessage(jsonObject);
                    break;
                case TYPE_EXPERIMENT_UPDATE_RESPONSE:
                    if (mExperimentCallback != null) {
                        mExperimentCallback.experimentUpdateResponseParsed();
                    }
                    break;
                case TYPE_LOGGING_START:
                    if (mLoggingCallback != null) {
                        mLoggingCallback.startLoggingMessageParsed();
                    }
                    break;
                case TYPE_LOGGING_START_RESPONSE:
                    if (mLoggingCallback != null) {
                        mLoggingCallback.startLoggingResponseParsed();
                    }
                case TYPE_LOGGING_STOP:
                    if (mLoggingCallback != null) {
                        mLoggingCallback.stopLoggingMessageParsed();
                    }
                    break;
                case TYPE_LOGGING_STOP_RESPONSE:
                    if (mLoggingCallback != null) {
                        mLoggingCallback.stopLoggingResponseParsed();
                    }
                default:
                    // do nothing!
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseExperimentUpdateMessage(JSONObject messageAsJsonObject) {
        HashMap<String, Boolean> manipStates = new HashMap<>();
        try {
            JSONArray manipulationStates = messageAsJsonObject.getJSONArray("manipulationStates");
            for (int i=0; i<manipulationStates.length(); i++) {
                JSONObject manipStateJSONObj = (JSONObject)manipulationStates.get(i);
                String manipString = manipStateJSONObj.getString("manipulationID");
                Boolean manipState = manipStateJSONObj.getBoolean("active");
                manipStates.put(manipString, manipState);
            }
            if (mExperimentCallback != null) {
                mExperimentCallback.experimentUpdateMessageParsed(manipStates);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static String generateExperimentUpdateJsonMessage(String experimentID,
                                                      HashMap<String,LocationManipulation> locationManipulationHashMap,
                                                      HashMap<String,OrientationManipulation> orientationManipulationHashMap) {
        // create json objects
        JSONObject experimentJson = new JSONObject();
        JSONArray manipulationsJson = new JSONArray();
        JSONObject tempManipulation;

        try {
            experimentJson.put(KEY_MESSAGE_TYPE, TYPE_EXPERIMENT_UPDATE);
            experimentJson.put("experimentID", experimentID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String,LocationManipulation> entry : locationManipulationHashMap.entrySet()) {
            tempManipulation = new JSONObject();
            try {
                tempManipulation.put("manipulationID", entry.
                        getValue().getId());
                tempManipulation.put("active", entry.getValue().getState());
                manipulationsJson.put(tempManipulation);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        for (Map.Entry<String,OrientationManipulation> entry : orientationManipulationHashMap.entrySet()) {
            tempManipulation = new JSONObject();
            try {
                tempManipulation.put("manipulationID", entry.getValue().getId());
                tempManipulation.put("active", entry.getValue().getState());
                manipulationsJson.put(tempManipulation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            experimentJson.put("manipulationStates", manipulationsJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return experimentJson.toString();
    }

    public static String generateExperimentUpdateResponse() {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put(KEY_MESSAGE_TYPE, TYPE_EXPERIMENT_UPDATE_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messageJson.toString();
    }

    public static String generateStartLoggingMessage() {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put(KEY_MESSAGE_TYPE, TYPE_LOGGING_START);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messageJson.toString();
    }

    public static String generateStopLoggingMessage() {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put(KEY_MESSAGE_TYPE, TYPE_LOGGING_STOP);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messageJson.toString();
    }

    public static String generateStartLoggingResponse() {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put(KEY_MESSAGE_TYPE, TYPE_LOGGING_START_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messageJson.toString();
    }

    public static String generateStopLoggingResponse() {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put(KEY_MESSAGE_TYPE, TYPE_LOGGING_STOP_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messageJson.toString();
    }
}
