package com.abistudy.flymetothemoon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import android.hardware.GeomagneticField;
import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.pm.PackageManager;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /* Variabili UI */
    private ImageView imageCompass;
    private TextView textViewHeading;
    private TextView textViewTrueHeading;
    private TextView textViewMagneticDeclination;

    /* Variabili Sensori */
    private SensorManager sensorManager;
    private float currentDegree = 0f;

    /* Array Reading e Calcoli */
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;

    // Richiesta permessi
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // Declinazione magnetica
    private float magneticDeclination = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inizializzazione UI
        imageCompass = findViewById(R.id.image_compass);
        textViewHeading = findViewById(R.id.text_view_heading);
        textViewTrueHeading = findViewById(R.id.text_view_true_heading);
        textViewMagneticDeclination = findViewById(R.id.text_view_magnetic_declination);

        // Inizializzazione SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Inizializzazione localizzazione
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inizializzazione permessi
        setupPermissionLauncher();
    }

    // Registrazione listener quando l'app è attiva
    @Override
    protected void onResume() {
        super.onResume();

        // Accelerometro
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        // Campo Magnetico
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME);
        }

        //Controlla permessi GPS
        checkLocationPermission();
    }


    // Ferma i sensori ad app chiusa
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Filtro low pass
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            CompassHelper.lowPassFilter(event.values.clone(), accelerometerReading);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            CompassHelper.lowPassFilter(event.values.clone(), magnetometerReading);
        }

        updateCompass();
    }


    private void updateCompass() {

        boolean success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading);

        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            float azimuthInRadians = orientationAngles[0];

            float headingMagnetic = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

            float headingTrue = headingMagnetic + magneticDeclination; // Semplice somma

            RotateAnimation rotateAnim = new RotateAnimation(
                    currentDegree,
                    -headingMagnetic,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            rotateAnim.setDuration(210);
            rotateAnim.setFillAfter(true);

            imageCompass.startAnimation(rotateAnim);
            currentDegree = -headingMagnetic;

            textViewHeading.setText(String.format("Mag Nord: %.0f°", headingMagnetic));
            textViewTrueHeading.setText(String.format("True North: %.0f°", headingTrue));
            textViewMagneticDeclination.setText(String.format("Declination: %.1f°", magneticDeclination));
        }
    }

    // Gestione dei Permessi
    private void setupPermissionLauncher() {
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        getLocationAndCalculateDeclination();
                    } else {
                        textViewMagneticDeclination.setText("Permission Denied");
                    }
                });
    }

    private void checkLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationAndCalculateDeclination();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // Calcolo della Declinazione
    @SuppressLint("MissingPermission")
    private void getLocationAndCalculateDeclination() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                lastKnownLocation = location;
                calculateMagneticDeclination();
            } else {
                textViewMagneticDeclination.setText("Position not found");
            }
        });
    }

    private void calculateMagneticDeclination() {
        if (lastKnownLocation != null) {
            GeomagneticField geoField = new GeomagneticField(
                    (float) lastKnownLocation.getLatitude(),
                    (float) lastKnownLocation.getLongitude(),
                    (float) lastKnownLocation.getAltitude(),
                    System.currentTimeMillis());

            magneticDeclination = geoField.getDeclination();
        }
    }
}