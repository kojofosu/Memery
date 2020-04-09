package com.mcdev.memery;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mcdev.memery.General.GetIntents;
import com.mcdev.memery.General.StringConstants;
import com.mcdev.memery.POJOS.Users;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class Login extends AppCompatActivity {
    //init custom made GetIntents
    GetIntents getIntents = new GetIntents();
    //init user class
    Users users = new Users();

    private static final String TAG = Login.class.getSimpleName();
    LottieAnimationView loginLottieAnimationView;
    VideoView loginVideoView;
    private String backgroundFile;

    //firebase
    FirebaseFirestore firestoreReference;
    FirebaseAuth firebaseAuth;

    //facebook Login
    LoginButton facebookLoginButton;
    CallbackManager facebookCallbackManager;
    //twitter login
    TwitterLoginButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init
        init();

        //init firebase stuff
        initFirebase();

        //check if user is already logged in to facebook
        isUserLoggedInToFacebook();
        //check if user is already logged in to twitter
        isUserLoggedIntoTwitter();

        firestoreReference.collection(StringConstants.LOGIN_BACKGROUND_COLLECTION).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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

    private void initFirebase() {
        //init firestore
        firestoreReference =  FirebaseFirestore.getInstance();
        //init firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void isUserLoggedIntoTwitter() {
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        boolean isLoggedIn = session != null;

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
                //saving user to firebase auth
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                String token = result.data.getAuthToken().token;
                String secret = result.data.getAuthToken().secret;
                AuthCredential authCredential = TwitterAuthProvider.getCredential(token, secret);
                firebaseAuth.signInWithCredential(authCredential)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d(TAG, "onSuccessFirebaseLogin: " + authResult);
                                //checking to see if user is a new user or not so that it doesn't add the user details more than once
                                if (authResult.getAdditionalUserInfo().isNewUser()){
                                    Log.d(TAG, "new user logged in");
                                    if (authResult.getUser() != null){
                                        //creating the document to get the ID and add it as a field in the users collection
                                        DocumentReference documentReference = firestoreReference.collection(StringConstants.USERS_COLLECTION).document();
                                        String documentID = documentReference.getId();

                                        //population user field
                                        users.setUserName(authResult.getUser().getDisplayName());
                                        users.setUserEmail(authResult.getUser().getEmail());
                                        users.setUserId(authResult.getUser().getUid());
                                        users.setUserPhoneNumber(authResult.getUser().getPhoneNumber());
                                        users.setUserPhotoUrl(authResult.getUser().getPhotoUrl().toString());
                                        users.setAccountCreation(authResult.getUser().getMetadata().getCreationTimestamp());
                                        users.setLastLogIn(authResult.getUser().getMetadata().getLastSignInTimestamp());
                                        users.setUserDocID(documentID);

                                        //pushing users details to firestore
                                        firestoreReference.collection(StringConstants.USERS_COLLECTION)
                                                .document(documentID)
                                                .set(users)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "onSuccessFirestoreLogin: " + aVoid);
                                                        //go to home page
                                                        getIntents.goToHome(Login.this);
                                                        Login.this.finish();
                                                        Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                }else {
                                    Log.d(TAG, "old user logged in");
                                    //go to home page
                                    getIntents.goToHome(Login.this);
                                    Login.this.finish();
                                    Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailureFirebaseLogin: " + e.getMessage() + "\n caused by : " + e.getCause());
                    }
                });

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
                //saving user to firebase auth
                AccessToken accessToken = loginResult.getAccessToken();
                AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
                //signing user in to firebase with credentials
                firebaseAuth.signInWithCredential(authCredential)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d(TAG, "onSuccessFirebaseLogin: " + authResult);
                                //checking to see if user is a new user or not so that it doesn't add the user details more than once
                                if (authResult.getAdditionalUserInfo().isNewUser()){
                                    Log.d(TAG, "new user logged in");
                                    if (authResult.getUser() != null){
                                        //creating the document to get the ID and add it as a field in the users collection
                                        DocumentReference documentReference = firestoreReference.collection(StringConstants.USERS_COLLECTION).document();
                                        String documentID = documentReference.getId();

                                        //population user field
                                        users.setUserName(authResult.getUser().getDisplayName());
                                        users.setUserEmail(authResult.getUser().getEmail());
                                        users.setUserId(authResult.getUser().getUid());
                                        users.setUserPhoneNumber(authResult.getUser().getPhoneNumber());
                                        users.setUserPhotoUrl(authResult.getUser().getPhotoUrl().toString());
                                        users.setAccountCreation(authResult.getUser().getMetadata().getCreationTimestamp());
                                        users.setLastLogIn(authResult.getUser().getMetadata().getLastSignInTimestamp());
                                        users.setUserDocID(documentID);

                                        //pushing users details to firestore
                                        firestoreReference.collection(StringConstants.USERS_COLLECTION)
                                                .document(documentID)
                                                .set(users)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "onSuccessFirestoreLogin: " + aVoid);
                                                        //go to home page
                                                        getIntents.goToHome(Login.this);
                                                        Login.this.finish();
                                                        Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                }else {
                                    Log.d(TAG, "old user logged in");
                                    //go to home page
                                    getIntents.goToHome(Login.this);
                                    Login.this.finish();
                                    Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailureFirebaseLogin: " + e.getMessage() + "\n caused by : " + e.getCause());
                    }
                });

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
