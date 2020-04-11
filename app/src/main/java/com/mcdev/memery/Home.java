package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.mcdev.memery.Adapters.ViewpagerAdapter;

public class Home extends AppCompatActivity {
    ChipNavigationBar chipNavigationBar;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //init
        init();

        //setting windows to fullscreen
        setWindowFullScreen();

        //init viewpager
        initViewpager();

        //listeners
        chipNavigationListener();

    }

    private void initViewpager() {
        ViewpagerAdapter adapter = new ViewpagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);       //initializing the view pager
        viewPager.setAdapter(adapter);      //setting view pager's adapter with contents in the viewpager adapter class
        viewPager.setOffscreenPageLimit(3);     //increasing the limit for screens to lose cache to three
        viewPager.setCurrentItem(0);        //setting default page to position 0
    }

    private void chipNavigationListener() {
        //when an item is on the chip navigation bar is clicked
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.memeries){
                    viewPager.setCurrentItem(0);        //setting current page to position 0
                    chipNavigationBar.setItemEnabled(R.id.memeries, true);
                    chipNavigationBar.getSelectedItemId();

                }
                if (i == R.id.other){
                    viewPager.setCurrentItem(1);        //setting current page to position 1
                    chipNavigationBar.setItemSelected(R.id.other,true);

                }
                if (i == R.id.me){
                    viewPager.setCurrentItem(2);        //setting current page to position 2
                    chipNavigationBar.setItemEnabled(R.id.me, true);

                }
            }
        });
    }

    private void setWindowFullScreen() {
        Window w = getWindow();     //initializing window
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);     //setting window flags to make status bar translucent
    }

    private void init() {
        chipNavigationBar = findViewById(R.id.chip_bottom_nav);
        viewPager = findViewById(R.id.fragment_container_viewpager);
    }
}
