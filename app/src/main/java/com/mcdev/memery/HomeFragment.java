package com.mcdev.memery;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import retrofit2.Call;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HomeFragment extends Fragment {

    FloatingActionButton homeFAB;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init
        init(view);

        //listeners
        fabListener();


        return view;
    }

    private void fabListener() {
        homeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AddMemeBottomSheetFragment addMemeBottomSheetFragment = new AddMemeBottomSheetFragment();
//                if (getFragmentManager() != null) {
//                    addMemeBottomSheetFragment.show(getFragmentManager(), addMemeBottomSheetFragment.getTag());
//                }
                startActivity(new Intent(getActivity(), AddMemeFromDeviceActivity.class));
            }
        });
    }


    private void init(@NotNull View view ) {
        homeFAB = view.findViewById(R.id.home_fab);
    }


}
