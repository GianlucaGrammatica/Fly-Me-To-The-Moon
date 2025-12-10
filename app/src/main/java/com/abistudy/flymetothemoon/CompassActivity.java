package com.abistudy.flymetothemoon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Vibrator;

public class CompassActivity extends AppCompatActivity implements SensorEventListener{

    // UI
    private ImageView imageCompass;
    private ImageView imageAstronaut;
    private TextView currentHeadingTextView;
    private TextView taget1TextView;
    private TextView taget2TextView;
    private TextView taget3TextView;
    private TextView astronautNameTextView;

    private SensorManager sensorManager;

    // Logica gioco
    private boolean gameActive = false;
    private boolean gameWon = false;
    private List<Integer> targetDegrees = new ArrayList<>();
    private AstronautsManager astronautsManager;
    private boolean[] targetReached = {false, false, false};
    private MediaPlayer startSound;
    private static final String PREFS_NAME = "fmttm_saves";
    private static final String KEY_SHIPMENTS = "shipmentCount";


    // Sensori
    private float[] gravityXYZ = new float[3];
    private float[] geomagneticXYZ = new float[3];
    private float currentDegree = 0f;
    Random random = new Random();
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        imageCompass = findViewById(R.id.image_compass);
        imageAstronaut = findViewById(R.id.image_astronaut);
        currentHeadingTextView = findViewById(R.id.text_view_current_heading);
        taget1TextView = findViewById(R.id.text_view_degree_1);
        taget2TextView = findViewById(R.id.text_view_degree_2);
        taget3TextView = findViewById(R.id.text_view_degree_3);
        astronautNameTextView = findViewById(R.id.text_view_astronaut_name);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        astronautsManager = new AstronautsManager(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        newGame();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // Sensore di accelerazione
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        // Sensore magnetico
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);

        if(gameWon) {
            gameWon = false;
            newGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Smette di sentire uando disattivato
        sensorManager.unregisterListener(this);

        if (startSound != null && startSound.isPlaying()) {
            startSound.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (startSound != null) {
            startSound.release();
            startSound = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Riduzione del rumore
        float alpha = 0.97f;

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            for(int i = 0; i < 3; i++){
                gravityXYZ[i] = alpha * gravityXYZ[i] + (1 - alpha) * event.values[i];
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            for(int i = 0; i < 3; i++){
                geomagneticXYZ[i] = alpha * geomagneticXYZ[i] + (1 - alpha) * event.values[i];
            }
        }

        float rotationMatrix[] = new float[9];
        float inclinationMatrix[] = new float[9];

        // SensorManager.getRotationMatrix usa i due vettori di sensori per calcolare R
        boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityXYZ, geomagneticXYZ);

        if(success) {
            float orientation[] = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            // Conversione in gradi
            float degree = (float) Math.toDegrees(orientation[0]);
            // normalizzazione
            degree = (degree + 360) % 360;

            // Animazione bussola
            RotateAnimation rotateAnimation = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(210);
            rotateAnimation.setFillAfter(true);

            imageCompass.startAnimation(rotateAnimation);
            currentDegree = -degree;

            // Testo
            currentHeadingTextView.setText(String.format("Current: %.0f°", degree));

            if(gameActive){
                checkTargetReached(degree);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void newGame(){

        if (startSound != null) {
            startSound.release();
            startSound = null;
        }

        startSound = MediaPlayer.create(this, R.raw.game_start);

        if (startSound != null) {
            startSound.start();

            startSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    startSound = null;
                }
            });
        }

        gameWon = false;

        for (int i = 0; i < targetReached.length; i++) {
            targetReached[i] = false;
        }

        targetDegrees.clear();
        for (int i = 0; i < 3; i++) {
            int newTarget;

            do {
                newTarget = random.nextInt(360);
            } while (targetDegrees.contains(newTarget));

            targetDegrees.add(newTarget);
        }

        astronautsManager.nextAstronaut();

        taget1TextView.setText(String.valueOf(targetDegrees.get(0)) + "°");
        taget2TextView.setText(String.valueOf(targetDegrees.get(1)) + "°");
        taget3TextView.setText(String.valueOf(targetDegrees.get(2)) + "°");
        taget1TextView.setAlpha(1f);
        taget2TextView.setAlpha(0.5f);
        taget3TextView.setAlpha(0.5f);
        taget1TextView.setTextColor(Color.parseColor("#FFFFFF"));
        taget2TextView.setTextColor(Color.parseColor("#FFFFFF"));
        taget3TextView.setTextColor(Color.parseColor("#FFFFFF"));

        imageAstronaut.setImageResource(astronautsManager.getCurrentImageResourceId());
        astronautNameTextView.setText(astronautsManager.currentAstronaut.getName());
        astronautNameTextView.setTextColor(astronautsManager.currentAstronaut.getColorInt());

        gameActive = true;
    }

    private void checkTargetReached(float currentHeading) {

        int currentTargetIndex = -1;
        for (int i = 0; i < targetReached.length; i++) {
            if (!targetReached[i]) {
                currentTargetIndex = i;
                break;
            }
        }

        if (currentTargetIndex == -1) {
            winGame();
            return;
        }


        float targetDegree = (float) targetDegrees.get(currentTargetIndex);

        float degreeDifference = Math.abs(currentHeading - targetDegree);

        float tolerance = 0.5f;

        if (degreeDifference <= tolerance) {

            targetReached[currentTargetIndex] = true;
            TextView currentTextView = null;

            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(100);
            }

            if (currentTargetIndex == 0) currentTextView = taget1TextView;
            else if (currentTargetIndex == 1) currentTextView = taget2TextView;
            else if (currentTargetIndex == 2) currentTextView = taget3TextView;

            if (currentTextView != null) {
                currentTextView.setTextColor(astronautsManager.currentAstronaut.getColorInt());
                currentTextView.setAlpha(1f);

                if (currentTargetIndex < 2) {
                    TextView nextTextView = (currentTargetIndex == 0) ? taget2TextView : taget3TextView;
                    nextTextView.setAlpha(1f);
                }
            }
        }
    }

    private void winGame(){
        gameActive = false;
        gameWon = true;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        int currentCount = settings.getInt(KEY_SHIPMENTS, 0);

        int newCount = currentCount + 1;

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(KEY_SHIPMENTS, newCount);

        editor.apply();

        Intent intent = new Intent(CompassActivity.this, RocketLaunchActivity.class);
        startActivity(intent);
    }
}