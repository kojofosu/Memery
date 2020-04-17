package com.mcdev.memery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jgabrielfreitas.core.BlurImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import org.jetbrains.annotations.NotNull;


/*TODO
 *  add custom progress bar for logging out*/
public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private BlurImageView profileBlurImageView;
    private ImageView profileImageView;
    private TextView profileUsernameTV, profileUserEmailTV;
    private ImageButton profileLogoutImageButton;
    //Picasso
    private  Picasso picasso;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        //init
        init(view);

        //get user info
        getUserInfo();

        //listeners
        logoutButtonListener();

        return view;
    }

    private boolean isUserLoggedInWithFacebook(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();      //getting access token
        return accessToken != null && !accessToken.isExpired();
    }

    private boolean isUserLoggedInWithTwitter(){
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();      //checking if user session is active
        //NOTE : if you want to get token and secret too use uncomment the below code
        /*TwitterAuthToken authToken = session.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;*/
        return session != null;
    }

    private void logoutButtonListener() {
        profileLogoutImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //inflating bottom sheet logout confirmation
                LogoutBottomSheetFragment logoutBottomSheetFragment = new LogoutBottomSheetFragment();
                if (getFragmentManager() != null) {
                    logoutBottomSheetFragment.show(getFragmentManager(), logoutBottomSheetFragment.getTag());
                }
            }
        });
    }


    private void getUserInfo() {

        //getting user info
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            String username = firebaseUser.getDisplayName();
            String userEmail = firebaseUser.getEmail();
            String userPhone = firebaseUser.getPhoneNumber();
            //checking to see if user is logged in with facebook or twitter so i can handle the profile images well to display in HD
            if (isUserLoggedInWithFacebook()){
                String userPhotoUrl = firebaseUser.getPhotoUrl().toString() + "?height=500";        //getting the HD version of user facebook profile image
                loadUserProfileImage(userPhotoUrl);     //loading the user's profile image
            }else if (isUserLoggedInWithTwitter()){
                String userPhotoUrl = firebaseUser.getPhotoUrl().toString().replace("_normal", "") ;        //getting the HD version of user twitter profile image
                loadUserProfileImage(userPhotoUrl);     //loading the user's profile image
            }



            profileUsernameTV.setText(username);    //setting user name text
            //checking to see if user email is null, then it assigns phone number..but if both are null set visibility to GONE
            if (userEmail != null){    //if email is not null and phone is null
                profileUserEmailTV.setText(userEmail);
            }else {   //if both email and phone are null
                profileUserEmailTV.setVisibility(View.GONE);
            }
        }
    }

    private void loadUserProfileImage(final String userPhotoUrl) {
        //getting user image
        picasso = Picasso.get();
        Log.d(TAG, "imgUrl " + userPhotoUrl);
        picasso.load(userPhotoUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profileImageView, new Callback() {
            @Override
            public void onSuccess() {
                picasso.load(userPhotoUrl).into(profileBlurImageView);
                profileBlurImageView.setBlur(7);
            }

            @Override
            public void onError(Exception e) {
                picasso.load(userPhotoUrl).into(profileImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        picasso.load(userPhotoUrl).into(profileBlurImageView);
                        profileBlurImageView.setBlur(7);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        });

    }

    private void init(@NotNull View view) {
        profileBlurImageView = view.findViewById(R.id.profile_blur_image_view);
        profileImageView = view.findViewById(R.id.profile_image_view);
        profileUsernameTV = view.findViewById(R.id.profile_user_name);
        profileUserEmailTV = view.findViewById(R.id.profile_user_email);
        profileLogoutImageButton = view.findViewById(R.id.profile_logout_image_button);
    }
}
