package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import com.mcdev.lazielibrary.Splashie;
import com.sarnava.textwriter.TextWriter;

import su.levenetc.android.textsurface.Text;
import su.levenetc.android.textsurface.TextBuilder;
import su.levenetc.android.textsurface.TextSurface;
import su.levenetc.android.textsurface.animations.Alpha;
import su.levenetc.android.textsurface.animations.CamRot;
import su.levenetc.android.textsurface.animations.ChangeColor;
import su.levenetc.android.textsurface.animations.Delay;
import su.levenetc.android.textsurface.animations.Just;
import su.levenetc.android.textsurface.animations.Loop;
import su.levenetc.android.textsurface.animations.Parallel;
import su.levenetc.android.textsurface.animations.Rotate3D;
import su.levenetc.android.textsurface.animations.Sequential;
import su.levenetc.android.textsurface.animations.ShapeReveal;
import su.levenetc.android.textsurface.animations.SideCut;
import su.levenetc.android.textsurface.animations.Slide;
import su.levenetc.android.textsurface.animations.TransSurface;
import su.levenetc.android.textsurface.contants.Align;
import su.levenetc.android.textsurface.contants.Axis;
import su.levenetc.android.textsurface.contants.Direction;
import su.levenetc.android.textsurface.contants.Side;

public class SplashScreen extends AppCompatActivity {
    TextWriter textWriter;
    TextSurface textSurface;

    com.mcdev.lazielibrary.SplashScreen kojofosuSplashScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        /*TEXT SURFACE*/
        textSurface = findViewById(R.id.textSurface);
        Text textMemery = TextBuilder
                .create("Memery")
                .setSize(64)
                .setAlpha(0)
                .setColor(Color.BLACK)
                .setPosition(Align.SURFACE_CENTER).build();

        textSurface.play(
                new Sequential(
                        Slide.showFrom(Side.LEFT, textMemery, 1000),
                        ShapeReveal.create(textMemery, 1000, SideCut.show(Side.LEFT), false),
                        new Parallel(ShapeReveal.create(textMemery, 600, SideCut.hide(Side.LEFT), false),
                                new Sequential(Delay.duration(300), ShapeReveal.create(textMemery, 600, SideCut.show(Side.LEFT), false)))

        ));


        /*TEXT WRITER*/
//        textWriter = findViewById(R.id.textWriter);
//
//        textWriter
//                .setWidth(12)
//                .setDelay(30)
//                .setColor(Color.argb(100,74, 72, 72))
//                .setConfig(TextWriter.Configuration.INTERMEDIATE)
//                .setSizeFactor(50f)
//                .setLetterSpacing(30f)
//                .setText("MEMERY")
//                .setListener(new TextWriter.Listener() {
//                    @Override
//                    public void WritingFinished() {
//
//                        //do stuff after animation is finished
//                    }
//                })
//                .startAnimation();

//        //creating splash screen
//        kojofosuSplashScreen = new com.mcdev.lazielibrary.SplashScreen(SplashScreen.this);
//        kojofosuSplashScreen.createSimpleSplashScreen(R.mipmap.ic_launcher_round, R.string.app_name, null);
//        kojofosuSplashScreen.setActivityBackgroundColor(R.color.colorAccent);
        //create splash
        Splashie.splash(SplashScreen.this, Login.class, 4);
    }

}
