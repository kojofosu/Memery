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
    ChipNavigationBar chipNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //init
        init();

        //setting windows to fullscreen
        setWindowFullScreen();

        //listeners
        chipNavigationListener();

    }

    private void chipNavigationListener() {
        //when an item is on the chip navigation bar is cliced
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

    private void setWindowFullScreen() {
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private void init() {
        chipNavigationBar = findViewById(R.id.chip_bottom_nav);
    }
}
