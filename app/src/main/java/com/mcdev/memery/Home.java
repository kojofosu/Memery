package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.mcdev.memery.General.GetIntents;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;

public class Home extends AppCompatActivity {
    TextView fblog, twlog;
    GetIntents getIntents = new GetIntents();

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fblog = findViewById(R.id.hello);
        FacebookSdk.fullyInitialize();
        fblog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                getIntents.goToLogin(Home.this);
                Home.this.finish();
            }
        });

        twlog = findViewById(R.id.hi);
        Twitter.getInstance();
        twlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                getIntents.goToLogin(Home.this);
                Home.this.finish();
            }
        });
    }
}
