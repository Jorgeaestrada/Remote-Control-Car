package com.controller.steeringwheel;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ImageButton retrocederBtn;
    private ImageButton acelerarBtn;
    private static final String HOST = "192.168.4.1";
    private static final String PORT = "80";
    private static int ACELERAR = 0;
    private static final String[] comandos = new String[]{
            "acelerar",
            "retroceder",
            "izquierda",
            "derecha"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        acelerarBtn = (ImageButton) findViewById(R.id.buttonUp);
        retrocederBtn = (ImageButton) findViewById(R.id.buttonDown);

        acelerarBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    System.out.println("Acelerando");
                    ACELERAR = 1;
                    sendRequest(HOST, PORT, comandos[0], ACELERAR);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println("Aceleracion pausada");
                    ACELERAR = 0;
                    sendRequest(HOST, PORT, comandos[0], ACELERAR);
                }
                return true;
            }
        });

        retrocederBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    public void sendRequest(String host, String port, String command, int value) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://" + host + ":" + port + "/" + command + "=" + value;
        System.out.println("comando:");
        System.out.println(url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("############################################");
                        System.out.println("Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Get rotation matrix
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);

        // Remap coordinate system
        float[] remappedRotationMatrix = new float[16];
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z,
                remappedRotationMatrix);

        // Convert to orientations
        float[] orientations = new float[3];
        SensorManager.getOrientation(remappedRotationMatrix, orientations);

        // Convert values in radian to degrees
        for (int i = 0; i < 3; i++) {
            orientations[i] = (float) (Math.toDegrees(orientations[i]));
        }

        if (orientations[2] >= -120 && orientations[2] < -100) {
            System.out.println("girando a la izquierda");
        } else if (orientations[2] >= -80 && orientations[2] < -60) {
            System.out.println("girando a la derecha");
        } else if (orientations[2] >= -100 && orientations[2] < -80) {
            System.out.println("auto centrado");
        }

        //Log.i("orientacion actual:\tX: ", String.valueOf(orientations[0]) + " Y: " + String.valueOf(orientations[1]) + " Z: " + String.valueOf(orientations[2]));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
