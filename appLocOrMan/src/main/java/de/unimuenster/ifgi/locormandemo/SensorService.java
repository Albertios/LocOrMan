package de.unimuenster.ifgi.locormandemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.sql.Timestamp;
import java.util.Date;

import de.unimuenster.ifgi.locormandemo.eventbus.OrientationUpdateEvent;

/*
The methods calculateAccMagOrientation, gyroFunction, getRotationMatrixFromOrientation,
matrixMultiplication, getRotationVectorFromGyro and calculateFusedOrientation are taken from
Android Sensor Fusion Tutorial by Paul Lawitzki
Source: http://www.thousand-thoughts.com/2012/03/android-sensor-fusion-tutorial/

Note: This class was originally created in the Mobile Indoor Localization study project and modified for this application.
 */
public class SensorService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagnetometer, mGyroscope;

    private float[] gyro = new float[3];
    private float[] accel = new float[3];
    private float[] magnet = new float[3];

    private float[] accMagOrientation = new float[3];
    private float[] fusedOrientation = new float[3];
    private float[] gyroOrientation = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] gyroMatrix = new float[9];

    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
    public static final float FILTER_COEFFICIENT = 0.98f;
    private float timestamp;
    private boolean initState = true;
    private static final int SLEEP_TIMER = 250;

    private long lastTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Register Listeners for the step counter, the accelerometer and the magnetometer services
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);

        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the sensor listeners
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);

        Log.d("SensorService destroyed", "");
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array and calculate orientation
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                // process gyro data
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }

        long time = new Date().getTime();
        long deltaTime = time - lastTime;

        if (deltaTime > SLEEP_TIMER) {
            calculateFusedOrientation();
            updateOrientation();
            lastTime = new Date().getTime();
        }
    }

    // calculates orientation angles from accelerometer and magnetometer output
    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    public void calculateFusedOrientation() {
        float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

        /*
         * Fix for 179° <--> -179° transition problem:
         * Check whether one of the two orientation angles (gyro or accMag) is negative while the
         * other one is positive.
         * If so, add 360° (2 * math.PI) to the negative value, perform the sensor fusion, and remove
         * the 360° from the result
         * if it is greater than 180°. This stabilizes the output in positive-to-negative-transition
         * cases.
         */

        for (int i = 0; i < 3; i++) {
            if (gyroOrientation[i] < -0.5 * Math.PI && accMagOrientation[i] > 0.0) {
                fusedOrientation[i] = (float) (FILTER_COEFFICIENT * (gyroOrientation[i] + 2.0 * Math.PI)
                        + oneMinusCoeff * accMagOrientation[i]);
                fusedOrientation[i] -= (fusedOrientation[i] > Math.PI) ? 2.0 * Math.PI : 0;
            } else if (accMagOrientation[i] < -0.5 * Math.PI && gyroOrientation[i] > 0.0) {
                fusedOrientation[i] = (float) (FILTER_COEFFICIENT * gyroOrientation[i] + oneMinusCoeff
                        * (accMagOrientation[i] + 2.0 * Math.PI));
                fusedOrientation[i] -= (fusedOrientation[i] > Math.PI) ? 2.0 * Math.PI : 0;
            } else {
                fusedOrientation[i] = FILTER_COEFFICIENT * gyroOrientation[i] + oneMinusCoeff
                        * accMagOrientation[i];
            }
        }

        // overwrite gyro matrix and orientation with fused orientation
        // to comensate gyro drift
        gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
        System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void updateOrientation() {
        float angle = (float) (fusedOrientation[0] * 180/Math.PI);

        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        // post data via eventbus
        EventBus.getDefault().post(new OrientationUpdateEvent(angle));
    }

}
