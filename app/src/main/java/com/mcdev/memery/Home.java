package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.mcdev.memery.General.GetIntents;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;

public class Home extends AppCompatActivity {
    TextView fblog, twlog;
    GetIntents getIntents = new GetIntents();

    ChipNavigationBar chipNavigationBar;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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

        chipNavigationBar = findViewById(R.id.chip_bottom_nav);
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.memeries){
                    chipNavigationBar.setItemEnabled(R.id.memeries, true);
                    chipNavigationBar.getSelectedItemId();

                }
                if (i == R.id.other){
                    chipNavigationBar.setItemSelected(R.id.other,true);
                }
                if (i == R.id.me){
                    chipNavigationBar.setItemEnabled(R.id.me, true);
                    //below code prevents white blank page when back button is pressed on on home page
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, new ProfileFragment());
                    transaction.disallowAddToBackStack();
                    transaction.commit();
                }
            }
        });
    }
}
