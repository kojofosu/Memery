package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.mcdev.memery.Adapters.ViewpagerAdapter;

import org.jetbrains.annotations.Nullable;

public class Home extends AppCompatActivity {
    private static final String TAG = Home.class.getSimpleName();
    ChipNavigationBar chipNavigationBar;
    NonSwipeableViewpager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //init
        init();

        //setting windows to fullscreen
        setWindowFullScreen();

        //init viewpager
//        initViewpager();

        //listeners
        chipNavigationListener();

        //getting data from share
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && type != null) {

            if (type.equals("text/plain")){
                String URL = intent.getStringExtra("android.intent.extra.TEXT");
                initViewpager(URL);
                viewPager.setCurrentItem(1);        //if its from a shareable content, launch the save fragment
                isChipItemSelected(R.id.save);      //if its from a shareable content, select the save fragment's chip
                if (URL != null) {
                    Log.println(Log.ASSERT,"shareableTextExtra",URL);
                }
                Log.d(TAG, "action : " + action);
                Log.d(TAG, "type : " + type);

                //passing the url to the fragment
                Bundle bundle = new Bundle();
                bundle.putString("tweetURL", URL);
                // set Fragment class Arguments
                SaveFragment saveFragment = new SaveFragment();
                saveFragment.setArguments(bundle);
            }


        }else{
            initViewpager(null);
            viewPager.setCurrentItem(0);        //if its not from a shareable content, launch the home fragment
            isChipItemSelected(R.id.memeries);      //if its not from a shareable content, select the home fragment's chip
        }

    }

    private void isChipItemSelected(final int getSelectedItemId) {
            Log.d(TAG, "chipNavigation bar isSelected : " + getSelectedItemId );
            chipNavigationBar.setItemSelected(getSelectedItemId, true);      //selecting the item first before timer starts

            final int interval = 2000;      // 2 Seconds before the item disables {using disabled because isSelected doesn't work for reasons i do not know}
            Handler handler = new Handler();        // init handler
            Runnable runnable = new Runnable() {
                public void run() {
                    final int interval = 1000;      // 1 Second before the item enables again for user to be able to click
                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        public void run() {
                            chipNavigationBar.setItemEnabled(getSelectedItemId, true);
                        }
                    };
                    handler.postAtTime(runnable, System.currentTimeMillis() + interval);        //chip enabler handler
                    handler.postDelayed(runnable, interval);

                    chipNavigationBar.setItemEnabled(getSelectedItemId, false);     //disabling chip item to reverse animation
                    Log.d(TAG, "chipNavigation bar getSelectedItemId : " + chipNavigationBar.getSelectedItemId());
                }
            };
            handler.postAtTime(runnable, System.currentTimeMillis() + interval);        //chip disabler handler
            handler.postDelayed(runnable, interval);
    }

    private void initViewpager(@Nullable String URL) {
        ViewpagerAdapter adapter = new ViewpagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, URL);       //initializing the view pager
        viewPager.setAdapter(adapter);      //setting view pager's adapter with contents in the viewpager adapter class
        viewPager.setOffscreenPageLimit(3);     //increasing the limit for screens to lose cache to three
//        viewPager.setCurrentItem(0);        //setting default page to position 0
    }

    private void chipNavigationListener() {
        //when an item is on the chip navigation bar is clicked
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.memeries){
                    viewPager.setCurrentItem(0);        //setting current page to position 0
                    isChipItemSelected(i);

                }
                if (i == R.id.save){
                    viewPager.setCurrentItem(1);        //setting current page to position 1
                   isChipItemSelected(i);

                }
                if (i == R.id.me){
                    viewPager.setCurrentItem(2);        //setting current page to position 2
                    isChipItemSelected(i);
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
