package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.mcdev.memery.General.GetIntents;

public class Home extends AppCompatActivity {
    TextView textView;
    GetIntents getIntents = new GetIntents();

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textView = findViewById(R.id.hello);
        FacebookSdk.fullyInitialize();
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                getIntents.goToLogin(Home.this);
                Home.this.finish();
            }
        });
    }
}
