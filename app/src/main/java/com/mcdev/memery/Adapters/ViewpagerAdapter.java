package com.mcdev.memery.Adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mcdev.memery.HomeFragment;
import com.mcdev.memery.SaveFragment;
import com.mcdev.memery.ProfileFragment;


public class ViewpagerAdapter extends FragmentPagerAdapter {

    private String url;

    public ViewpagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public ViewpagerAdapter(@NonNull FragmentManager fm, int behavior, String url) {
        super(fm, behavior);
        this.url = url;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new HomeFragment();      //creating instance of Home Fragment
            case 1:
//                return new SaveFragment();     //creating instance of Save Fragment
                SaveFragment saveFragment = new SaveFragment();
                Bundle bundle = new Bundle();
                bundle.putString("tweetURL", url);
                // set Fragmentclass Arguments
                saveFragment.setArguments(bundle);
                return saveFragment;
            case 2:
                return new ProfileFragment();       //creating instance of Profile Fragment
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;       //Three fragments
    }
}
