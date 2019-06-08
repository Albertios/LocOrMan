package de.unimuenster.ifgi.locormandemo.manipulations;

import static de.unimuenster.ifgi.locormandemo.manipulations.ExperimentProvider.MyClass.dispAccManipValue;
import static de.unimuenster.ifgi.locormandemo.manipulations.ExperimentProvider.MyClass.orManipRecency01Value;
import static de.unimuenster.ifgi.locormandemo.manipulations.ExperimentProvider.MyClass.orManipSystAcc01Value;
import static de.unimuenster.ifgi.locormandemo.manipulations.ExperimentProvider.MyClass.systAccManipVaule;

/**
 * Created by sven on 30.06.16.
 */
public class ExperimentProvider {
    private static final ExperimentProvider experimentProviderSingleton = new ExperimentProvider();
    private Experiment dummyExperiment = new Experiment("exp01", "Pretest experiment");



    private ExperimentProvider() {
        initializePretestExperiment();
    }

    public static ExperimentProvider getInstance() {
        return experimentProviderSingleton;
    }

    public Experiment getExperimentWithID(String id) {
        if (id.equals(dummyExperiment.getId())) {
            return dummyExperiment;
        } else {
            return null;
        }
    }




/*

    private void initializeDummyExperiment() {

        // location manipulations
        LocationManipulation systAccManip = new LocationManipulation("locManSystAcc01", "Systematic location accuracy: 10 m", false, "10", Manipulation.Dimension.SPACE, Manipulation.Type.ACCURACY);
        LocationManipulation dispAccManip = new LocationManipulation("locManDispAcc01", "Display location accuracy: 30 m", false, "30", Manipulation.Dimension.SPACE, Manipulation.Type.DISPLAY_ACCURACY);
        LocationManipulation coverManip = new LocationManipulation("locManCov01", "No location coverage", false, "", Manipulation.Dimension.SPACE, Manipulation.Type.NO_COVERAGE);
        LocationManipulation updateRateManip01 = new LocationManipulation("locManUpdateRate01", "Update rate: 30 s", false, "30", Manipulation.Dimension.TIME, Manipulation.Type.UPDATE_RATE);
        LocationManipulation updateRateManip02 = new LocationManipulation("locManUpdateRate02", "Update rate: 10 s", false, "10", Manipulation.Dimension.TIME, Manipulation.Type.UPDATE_RATE);
        LocationManipulation recencyManip01 = new LocationManipulation("locManRecency01", "Delay: 30 seconds", false, "30", Manipulation.Dimension.TIME, Manipulation.Type.RECENCY);
        LocationManipulation recencyManip02 = new LocationManipulation("locManRecency02", "Delay: 10 seconds", false, "10", Manipulation.Dimension.TIME, Manipulation.Type.RECENCY);
        LocationManipulation granularityManip01 = new LocationManipulation("locManGran01", "Location granularity 50 m", false, "50", Manipulation.Dimension.SPACE, Manipulation.Type.GRANULARITY);

        dummyExperiment.addLocationManipulation(systAccManip);
        dummyExperiment.addLocationManipulation(dispAccManip);
        dummyExperiment.addLocationManipulation(coverManip);
        dummyExperiment.addLocationManipulation(updateRateManip01);
        dummyExperiment.addLocationManipulation(updateRateManip02);
        dummyExperiment.addLocationManipulation(recencyManip01);
        dummyExperiment.addLocationManipulation(recencyManip02);
        dummyExperiment.addLocationManipulation(granularityManip01);

        // orientation manipulations
        // systematic accuracy
        OrientationManipulation orManipSystAcc01 = new OrientationManipulation("orManSystAcc01", "Orientation accuracy: 90", false, "90", Manipulation.Dimension.SPACE, Manipulation.Type.ACCURACY);
        OrientationManipulation orManipSystAcc02 = new OrientationManipulation("orManSystAcc02", "Orientation accuracy: 180", false, "180", Manipulation.Dimension.SPACE, Manipulation.Type.ACCURACY);
        // coverage
        OrientationManipulation orManipCov = new OrientationManipulation("orManipCov", "No orientation coverage", false, "", Manipulation.Dimension.SPACE, Manipulation.Type.NO_COVERAGE);
        // update rate
        OrientationManipulation orManipUpdateRate01 = new OrientationManipulation("orManipUpdateRate01", "Orientation update rate: 1 s", false, "1", Manipulation.Dimension.TIME, Manipulation.Type.UPDATE_RATE);
        OrientationManipulation orManipUpdateRate02 = new OrientationManipulation("orManipUpdateRate02", "Orientation update rate: 3 s", false, "3", Manipulation.Dimension.TIME, Manipulation.Type.UPDATE_RATE);
        OrientationManipulation orManipUpdateRate03 = new OrientationManipulation("orManipUpdateRate03", "Orientation update rate: 0.5 s", false, "0.5", Manipulation.Dimension.TIME, Manipulation.Type.UPDATE_RATE);
        // recency
        OrientationManipulation orManipRecency01 = new OrientationManipulation("orManipRecency01", "Orientation delay: 1 s", false, "1", Manipulation.Dimension.TIME, Manipulation.Type.RECENCY);
        OrientationManipulation orManipRecency02 = new OrientationManipulation("orManipRecency02", "Orientation delay: 5 s", false, "5", Manipulation.Dimension.TIME, Manipulation.Type.RECENCY);
        OrientationManipulation orManipRecency03 = new OrientationManipulation("orManipRecency03", "Orientation delay: 0.5 s", false, "0.5", Manipulation.Dimension.TIME, Manipulation.Type.RECENCY);
        // granularity
        OrientationManipulation orManipGran01 = new OrientationManipulation("orManipGran01", "Orientation granularity: 4", false, "4", Manipulation.Dimension.SPACE, Manipulation.Type.GRANULARITY);
        OrientationManipulation orManipGran02 = new OrientationManipulation("orManipGran02", "Orientation granularity: 8", false, "8", Manipulation.Dimension.SPACE, Manipulation.Type.GRANULARITY);

        dummyExperiment.addOrientationManipulation(orManipSystAcc01);
        dummyExperiment.addOrientationManipulation(orManipSystAcc02);
        dummyExperiment.addOrientationManipulation(orManipCov);
        dummyExperiment.addOrientationManipulation(orManipGran01);
        dummyExperiment.addOrientationManipulation(orManipGran02);
        dummyExperiment.addOrientationManipulation(orManipUpdateRate01);
        dummyExperiment.addOrientationManipulation(orManipUpdateRate02);
        dummyExperiment.addOrientationManipulation(orManipUpdateRate03);
        dummyExperiment.addOrientationManipulation(orManipRecency01);
        dummyExperiment.addOrientationManipulation(orManipRecency02);
        dummyExperiment.addOrientationManipulation(orManipRecency03);

    }

*/



public static class MyClass {

    public static String orManipSystAcc01Value = "180";          // 180° ungenau
    public static String dispAccManipValue = "30";          // 30 meter radius
    public static  String systAccManipVaule = "20";           // 20 meter umkreis
    public static  String recencyManip02Value = "15";           // 15 secunden
    public static  String orManipRecency01Value = "2";            // 2 secunden


}






    private void initializePretestExperiment() {
        LocationManipulation coverManip = new LocationManipulation("locManCov01", "1. location: no coverage", false, "", Manipulation.Dimension.SPACE, Manipulation.Type.NO_COVERAGE);
        OrientationManipulation orManipSystAcc01 = new OrientationManipulation("orManSystAcc01", "2. orientation: low accuracy ("+ orManipSystAcc01Value +"°)", false, ""+MyClass.orManipSystAcc01Value+"", Manipulation.Dimension.SPACE, Manipulation.Type.ACCURACY);
        LocationManipulation dispAccManip = new LocationManipulation("locManDispAcc01", "3. location: display accuracy (" + dispAccManipValue + " m)", false, ""+MyClass.dispAccManipValue+"" , Manipulation.Dimension.SPACE, Manipulation.Type.DISPLAY_ACCURACY);
        LocationManipulation systAccManip = new LocationManipulation("locManSystAcc01", "4. location: low accuracy ("+ systAccManipVaule+" m)", false, ""+MyClass.systAccManipVaule, Manipulation.Dimension.SPACE, Manipulation.Type.ACCURACY);
        OrientationManipulation orManipCov = new OrientationManipulation("orManipCov", "5. orientation: no coverage", false, "", Manipulation.Dimension.SPACE, Manipulation.Type.NO_COVERAGE);
        LocationManipulation recencyManip02 = new LocationManipulation("locManRecency02", "6. location: delay ("+MyClass.recencyManip02Value+" s)", false, "" +MyClass.recencyManip02Value, Manipulation.Dimension.TIME, Manipulation.Type.RECENCY);
        OrientationManipulation orManipRecency01 = new OrientationManipulation("orManipRecency01", "7. orientation: delay ("+ orManipRecency01Value +" s)", false, ""+MyClass.orManipRecency01Value, Manipulation.Dimension.TIME, Manipulation.Type.RECENCY);

        dummyExperiment.addLocationManipulation(coverManip);
        dummyExperiment.addOrientationManipulation(orManipSystAcc01);
        dummyExperiment.addLocationManipulation(dispAccManip);
        dummyExperiment.addLocationManipulation(systAccManip);
        dummyExperiment.addOrientationManipulation(orManipCov);
        dummyExperiment.addLocationManipulation(recencyManip02);
        dummyExperiment.addOrientationManipulation(orManipRecency01);
    }



}
