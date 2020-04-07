package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    LottieAnimationView loginLottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        init();

        //load video on videoView
        lottieAnimation();
    }

    private void lottieAnimation() {
        loginLottieAnimationView.playAnimation();
    }


    private void init() {
        loginLottieAnimationView = findViewById(R.id.login_lottie_animation_view);
    }
}
