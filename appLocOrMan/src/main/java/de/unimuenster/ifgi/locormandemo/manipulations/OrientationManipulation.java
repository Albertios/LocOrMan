package de.unimuenster.ifgi.locormandemo.manipulations;

/**
 * Created by sven on 23.06.16.
 */
public class OrientationManipulation extends Manipulation {
    private double value;

    /**
     * Creates a new OrientationManipulation object without specifying a display name.
     * @param id is the ID of the manipulation
     * @param state is a boolean that reflects if the manipulation should be active from the beginning or not
     * @param value is a numeric value that specifies the behaviour of the manipulation (e.g. radius for accuracy manipulation)
     * @param dimension is of type Dimension: Manipulation in space or time
     * @param type of type Type (e.g. accuracy, granularity etc.)
     */
    public OrientationManipulation(String id, boolean state, String value, Dimension dimension, Type type) {
        this.id = id;
        this.state = state;
        this.dimension = dimension;
        this.type = type;
        this.value = convertValue(value); // cast string
    }

    /**
     * Creates a new OrientationManipulation object with specifying a display name.
     * @param id is the ID of the manipulation
     * @param name is the name to be displayed
     * @param state is a boolean that reflects if the manipulation should be active from the beginning or not
     * @param value is a numeric value that specifies the behaviour of the manipulation (e.g. radius for accuracy manipulation)
     * @param dimension is of type Dimension: Manipulation in space or time
     * @param type of type Type (e.g. accuracy, granularity etc.)
     */
    public OrientationManipulation(String id, String name, boolean state, String value, Dimension dimension, Type type) {
        this(id, state, value, dimension, type);
        this.name = name;
    }

    /**
     * Converts the value of a location manipulation from string to double
     * @param value is the manipulation value as a String
     * @return the double representation of the value
     */
    private double convertValue(String value) {
        double returnValue = -1.0;
        if (this.type != Type.NO_COVERAGE) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }
        return returnValue;
    }

    public double getValue() {
        return value;
    }

}
