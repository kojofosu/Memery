package com.mcdev.memery.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mcdev.memery.HomeFragment;
import com.mcdev.memery.SaveFragment;
import com.mcdev.memery.ProfileFragment;


public class ViewpagerAdapter extends FragmentPagerAdapter {


    public ViewpagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new HomeFragment();      //creating instance of Home Fragment
            case 1:
                return new SaveFragment();     //creating instance of Other Fragment
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
