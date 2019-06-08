package de.unimuenster.ifgi.locormandemo.filter;

/**
 * Created by sven on 28.07.16.
 * This class encapsulates orientation information with angle from north and timestamp.
 */
public class Orientation {
    private double angle;
    private long timestamp;

    public Orientation(double angle) {
        this.setAngle(angle);
        this.timestamp = System.currentTimeMillis();
    }

    public Orientation(double angle, long timestamp) {
        this.setAngle(angle);
        this.timestamp = timestamp;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        if (angle < 0.0) {
            this.angle = 360.0 + angle;
        } else {
            this.angle = angle;
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
