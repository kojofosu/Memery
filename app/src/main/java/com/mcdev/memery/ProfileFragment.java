package com.mcdev.memery;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jgabrielfreitas.core.BlurImageView;
import com.mcdev.memery.General.GetIntents;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import static com.facebook.FacebookSdk.getApplicationContext;


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
    //Firebase
    private FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;


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

        //init Firebase stuff
        initFirebaseStuff();

        //get user info
        getUserInfo();

        //listeners
        logoutButtonListener(view);

        return view;
    }

    private boolean isUserLoggedInWithFacebook(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();      //getting access token
        return accessToken != null && !accessToken.isExpired();
    }

    private boolean isUserLoggedInWithTwitter(){
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();      //checking if user session is active
        return session != null;
    }

    private void logoutButtonListener(final View getView) {
        profileLogoutImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //create an alert dialog for user to confirm if they want to really logout
                final AlertDialog.Builder builder;

                builder = new AlertDialog.Builder(getView.getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setTitle("Logout?")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
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
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        //.setIcon(R.drawable.ic_bubble_chart_black_24dp)
                        .show();
            }
        });
    }

    private void SendUserToLoginActivity(View view) {
        GetIntents getIntents = new GetIntents();
        getIntents.goToLogin(getActivity());
        getActivity().finish();
    }

    private void getUserInfo() {
        //getting user image
        picasso = Picasso.get();
        firebaseFirestore.collection("samplePic").document("123").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()){
                    Log.d(TAG, "login background empty");
                }else {
                    final String imgUrl = documentSnapshot.get("imageUrl").toString();
                    Log.d(TAG, "imgUrl " + imgUrl);
                    picasso.load(imgUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profileImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            picasso.load(imgUrl).into(profileBlurImageView);
                            profileBlurImageView.setBlur(20);
                        }

                        @Override
                        public void onError(Exception e) {
                            picasso.load(imgUrl).into(profileImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    picasso.load(imgUrl).into(profileBlurImageView);
                                    profileBlurImageView.setBlur(20);
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        }
                    });
                }
            }
        });

        //getting user info
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            String username = firebaseUser.getDisplayName();
            String userEmail = firebaseUser.getEmail();
            String userPhone = firebaseUser.getPhoneNumber();
            String userPhotoUrl = firebaseUser.getPhotoUrl().toString();

            profileUsernameTV.setText(username);    //setting user name text
            //checking to see if user email is null, then it assigns phone number..but if both are null set visibility to GONE
            if (userEmail != null){    //if email is not null and phone is null
                profileUserEmailTV.setText(userEmail);
            }else {   //if both email and phone are null
                profileUserEmailTV.setVisibility(View.GONE);
            }
        }
    }

    private void initFirebaseStuff() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void init(View view) {
        profileBlurImageView = view.findViewById(R.id.profile_blur_image_view);
        profileImageView = view.findViewById(R.id.profile_image_view);
        profileUsernameTV = view.findViewById(R.id.profile_user_name);
        profileUserEmailTV = view.findViewById(R.id.profile_user_email);
        profileLogoutImageButton = view.findViewById(R.id.profile_logout_image_button);
    }
}
