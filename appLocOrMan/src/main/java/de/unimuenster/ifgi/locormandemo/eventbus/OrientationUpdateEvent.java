package de.unimuenster.ifgi.locormandemo.eventbus;

/**
 * Created by sven on 01.07.16.
 */
public class OrientationUpdateEvent {
    public final double angleInDegrees;

    public OrientationUpdateEvent(double angleInDegrees) {
        this.angleInDegrees = angleInDegrees;
    }
}
