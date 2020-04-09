package com.mcdev.memery;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mcdev.memery.General.GetIntents;
import com.mcdev.memery.General.StringConstants;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class Login extends AppCompatActivity {
    //init custom made GetIntents
    GetIntents getIntents = new GetIntents();

    private static final String TAG = Login.class.getSimpleName();
    LottieAnimationView loginLottieAnimationView;
    VideoView loginVideoView;
    FirebaseFirestore loginBackgroundFirestoreReference;
    private String backgroundFile;

    //facebook Login
    LoginButton facebookLoginButton;
    CallbackManager facebookCallbackManager;
    //twitter login
    TwitterLoginButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        init();

        //check if user is already logged in to facebook
        isUserLoggedInToFacebook();
        //check if user is already logged in to twitter
        isUserLoggedIntoTwitter();
        //init firestore
        loginBackgroundFirestoreReference =  FirebaseFirestore.getInstance();
        loginBackgroundFirestoreReference.collection(StringConstants.LOGIN_BACKGROUND_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()){
                    Log.d(TAG, "login background empty");
                }else {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){

                        switch (documentChange.getType()){
                            case ADDED:
                                //getting the string value for the background file
                                backgroundFile = documentChange.getDocument().getString("file");
                                Log.d(TAG, "login background value : " + backgroundFile);
                                //load video on video view
                                loadVideoVideo(backgroundFile);
                                break;
                            case MODIFIED:
                            case REMOVED:
                                break;
                        }
                    }

                }
            }
        });

        //listeners
        facebookLoginStuff();
        twitterLoginStuff();
    }

    private void isUserLoggedIntoTwitter() {
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        TwitterAuthToken authToken = session.getAuthToken();
        boolean isLoggedIn = authToken != null && !authToken.isExpired();

        if (isLoggedIn){
            getIntents.goToHome(Login.this);
            Login.this.finish();
        }

        //NOTE : if you want to get token and secret too use uncomment the below code
        /*TwitterAuthToken authToken = session.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;*/
    }

    private void twitterLoginStuff() {
        //twitter login button
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "onSuccessTwitterLogin: " + result);
                Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                //go to home page
                getIntents.goToHome(Login.this);
                Login.this.finish();
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "onErrorTwitterLogin: " + exception.getMessage() + "\n caused by :" + exception.getCause());
            }
        });
    }

    private void isUserLoggedInToFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn){
            getIntents.goToHome(Login.this);
            Login.this.finish();
        }
    }

    private void facebookLoginStuff() {
        //facebook login button
        facebookCallbackManager = CallbackManager.Factory.create(); //creating facebook callback
        facebookLoginButton.setPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccessFacebookLogin: " + loginResult);
                Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                //go to home page
                getIntents.goToHome(Login.this);
                Login.this.finish();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancelFacebookLogin: " + "Log in cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onErrorFacebookLogin: " + error.getMessage() + "\n caused by :" + error.getCause());
            }
        });
    }

    private void loadVideoVideo(String backgroundFile) {
        Log.d(TAG, "new log " + backgroundFile);
        loginVideoView.setVideoPath(backgroundFile);        //setting video path
        loginVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //setting looping to true
                mediaPlayer.setLooping(true);
                //Get your video's width and height
                int videoWidth = mediaPlayer.getVideoWidth();
                int videoHeight = mediaPlayer.getVideoHeight();

                //Get VideoView's current width and height
                int videoViewWidth = loginVideoView.getWidth();
                int videoViewHeight = loginVideoView.getHeight();

                float xScale = (float) videoViewWidth / videoWidth;
                float yScale = (float) videoViewHeight / videoHeight;

                //For Center Crop use the Math.max to calculate the scale
                //float scale = Math.max(xScale, yScale);
                //For Center Inside use the Math.min scale.
                //I prefer Center Inside so I am using Math.min
                float scale = Math.max(xScale, yScale);

                float scaledWidth = scale * videoWidth;
                float scaledHeight = scale * videoHeight;

                //Set the new size for the VideoView based on the dimensions of the video
                ViewGroup.LayoutParams layoutParams = loginVideoView.getLayoutParams();
                layoutParams.width = (int)scaledWidth;
                layoutParams.height = (int)scaledHeight;
                loginVideoView.setLayoutParams(layoutParams);
            }
        });
        loginVideoView.start(); //start playing video
    }


    private void init() {
        loginVideoView = findViewById(R.id.login_video_view);
        facebookLoginButton = findViewById(R.id.facebook_login_btn);
        twitterLoginButton = findViewById(R.id.twitter_login_btn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //facebook onActivityResult
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        //twitter onActivityResult
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }
}
