package com.abistudy.flymetothemoon;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;

public class WelcomeActivity extends AppCompatActivity {

    Button startButton;
    private TextView shipmentCountTextView;
    private MediaPlayer introSoundPlayer;

    private static final String PREFS_NAME = "fmttm_saves";
    private static final String KEY_SHIPMENTS = "shipmentCount";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        startButton = findViewById(R.id.startButton);
        shipmentCountTextView = findViewById(R.id.shipmentCountTextView);

        introSoundPlayer = MediaPlayer.create(this, R.raw.ui_sound_1);

        if (introSoundPlayer != null) {
            introSoundPlayer.start();

            introSoundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    introSoundPlayer = null;
                }
            });
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, CompassActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateShipmentCount();
    }

    private void updateShipmentCount() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        int count = settings.getInt(KEY_SHIPMENTS, 0);

        shipmentCountTextView.setText("Total Shipments: " + count + " 🚀");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (introSoundPlayer != null) {
            introSoundPlayer.release();
            introSoundPlayer = null;
        }
    }
}