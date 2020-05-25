package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.mcdev.lazielibrary.Splashie;

public class SplashScreen extends AppCompatActivity {

    com.mcdev.lazielibrary.SplashScreen kojofosuSplashScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash_screen);


        //creating splash screen
        kojofosuSplashScreen = new com.mcdev.lazielibrary.SplashScreen(SplashScreen.this);
        kojofosuSplashScreen.createSimpleSplashScreen(R.mipmap.ic_launcher_round, R.string.app_name, null);
        kojofosuSplashScreen.setActivityBackgroundColor(R.color.colorAccent);
        //create splash
        Splashie.splash(SplashScreen.this, Login.class, 4);
    }

}
