package com.mcdev.memery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.mcdev.memery.General.GetIntents;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import org.jetbrains.annotations.NotNull;


/**
 * A simple {@link Fragment} subclass.
 */
public class LogoutBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = LogoutBottomSheetFragment.class.getSimpleName();
    private Button confirmLogoutBtn, cancelLogoutBtn;
    private FirebaseAuth firebaseAuth;
    public LogoutBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logout_bottom_sheet, container, false);

        //init
        init(view);

        //init Firebase stuff
        initFirebaseStuff();

        //listeners
        confirmLogoutListener();
        cancelLogoutListener();
        return view;
    }

    private void cancelLogoutListener() {
        cancelLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void confirmLogoutListener() {
        confirmLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // continue with logout
                try {
                    //checking to see if user is logged in with twitter or facebook
                    if (isUserLoggedInWithFacebook()){
                        Log.d(TAG, "isUserLoggedInWithFacebook : " + isUserLoggedInWithFacebook());
                        //then log user out of facebook
                        FacebookSdk.fullyInitialize();      //initializing facebook SDK
                        // remove permissions and revoke access for user to be able to login again with another account if they choose
                        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                LoginManager.getInstance().logOut();        //Log user out of facebook
                                firebaseAuth.signOut();     //log user out of firebase
                                SendUserToLoginActivity(view);      //send the user to login page
                            }
                        }).executeAsync();      //execute permission deletion
                    }else if (isUserLoggedInWithTwitter()){
                        Log.d(TAG, "isUserLoggedInWithTwitter : " + isUserLoggedInWithTwitter());
                        //then log user out of twitter
                        TwitterCore.getInstance().getSessionManager().clearActiveSession();     //clearing current user session
                        firebaseAuth.signOut();     //log user out of firebase
                        SendUserToLoginActivity(view);      //send the user to login page
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    private void SendUserToLoginActivity(View view) {
        GetIntents getIntents = new GetIntents();
        getIntents.goToLogin(getActivity());
        getActivity().finish();
    }

    private boolean isUserLoggedInWithFacebook(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();      //getting access token
        return accessToken != null && !accessToken.isExpired();
    }

    private void initFirebaseStuff() {
        firebaseAuth = FirebaseAuth.getInstance();      //initializing firebase auth
    }

    private boolean isUserLoggedInWithTwitter(){
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();      //checking if user session is active
        return session != null;
    }

    private void init(View view) {
        confirmLogoutBtn = view.findViewById(R.id.confirm_logout);
        cancelLogoutBtn = view.findViewById(R.id.cancel_logout);
    }
}
