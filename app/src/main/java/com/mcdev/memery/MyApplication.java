package com.mcdev.memery;

import android.app.Application;
import android.util.Log;

import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))   //enables logging when app is in debug mode
                .twitterAuthConfig(new TwitterAuthConfig(getResources().getString(R.string.twitter_app_id), getResources().getString(R.string.twitter_app_secret)))     //passed the app ID and secret respectively
                .debug(true)    //enables debug mode on
                .build();

        //initializing twitter with above config
        Twitter.initialize(config);
    }
}
