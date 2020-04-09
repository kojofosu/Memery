package com.mcdev.memery.General;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.mcdev.memery.Home;

public class GetIntents{
    public void goToHome(Context context){
        Intent intent = new Intent(context, Home.class);
        context.startActivity(intent);
    }
}
