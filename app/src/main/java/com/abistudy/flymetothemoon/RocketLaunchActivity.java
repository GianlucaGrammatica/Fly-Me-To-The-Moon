package com.abistudy.flymetothemoon;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class RocketLaunchActivity extends AppCompatActivity {

    private VideoView rocketVideoView;
    private Vibrator vibrator;
    private MediaPlayer woosh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rocket_launch);

        woosh = MediaPlayer.create(this, R.raw.woosh);

        rocketVideoView = findViewById(R.id.rocketVideoView);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.rocket_video);
        rocketVideoView.setVideoURI(videoUri);

        rocketVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });

        rocketVideoView.start();
        if (woosh != null) {
            woosh.start();

            woosh.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    woosh = null;
                }
            });
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(1700);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (rocketVideoView.isPlaying()) {
            rocketVideoView.stopPlayback();
        }
        if (woosh != null) {
            woosh.release();
            woosh = null;
        }
    }
}