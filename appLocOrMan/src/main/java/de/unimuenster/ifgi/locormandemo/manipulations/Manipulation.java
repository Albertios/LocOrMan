package de.unimuenster.ifgi.locormandemo.manipulations;

/**
 * Created by sven on 23.06.16.
 */
public class Manipulation {
    protected boolean state;
    protected String id;
    protected String name = "";
    protected Dimension dimension;
    protected Type type;
    private String jsonString = "";


    public enum Dimension {
        SPACE, TIME
    }

    public enum Type {
        ACCURACY, PRECISION, GRANULARITY, DISPLAY_ACCURACY, NO_COVERAGE, UPDATE_RATE, RECENCY
    }

    public Manipulation() {

    }

    public Manipulation(String id) {
        this.id = id;
        this.state = false;
    }

    public Manipulation(String id, boolean state) {
        this(id);
        this.state = state;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Type getType() {
        return type;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
