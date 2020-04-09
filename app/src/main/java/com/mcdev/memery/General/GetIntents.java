package com.mcdev.memery.General;

import android.content.Context;
import android.content.Intent;

import com.mcdev.memery.Home;
import com.mcdev.memery.Login;

public class GetIntents{
    public void goToHome(Context context){
        Intent intent = new Intent(context, Home.class);
        context.startActivity(intent);
    }
        public void goToLogin(Context context){
        Intent intent = new Intent(context, Login.class);
        context.startActivity(intent);
    }

}
