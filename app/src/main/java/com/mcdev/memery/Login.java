package com.mcdev.memery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.internal.CallbackManagerImpl;
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
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.vincan.medialoader.DefaultConfigFactory;
import com.vincan.medialoader.DownloadManager;
import com.vincan.medialoader.MediaLoader;
import com.vincan.medialoader.MediaLoaderConfig;
import com.vincan.medialoader.data.file.naming.HashCodeFileNameCreator;
import com.vincan.medialoader.download.DownloadListener;

import java.io.File;
import java.util.Objects;

/*TODO
*  add custom progress bar when logging in*/
public class Login extends AppCompatActivity {
    //init custom made GetIntents
    GetIntents getIntents = new GetIntents();
    //init user class
    Users users = new Users();

    private static final String TAG = Login.class.getSimpleName();
    VideoView loginVideoView;

    //firebase
    FirebaseFirestore firestoreReference;
    FirebaseAuth firebaseAuth;

    //facebook Login
    LoginButton facebookLoginButton;
    CallbackManager facebookCallbackManager;
    //twitter login
    TwitterLoginButton twitterLoginButton;

    MediaLoader mediaLoader;

    LottieDialogFragment lottieDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init
        init();

        //init custom dialog
        lottieDialogFragment = new LottieDialogFragment();

        //init firebase stuff
        initFirebase();

        //making activity fullscreen
        setWindowFullScreen();

        isUserLoggedIntoFirebase();

        //video cache
        videoCaching();

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
                                String backgroundFile = documentChange.getDocument().getString("file");
                                Log.d(TAG, "login background video file (added) : " + backgroundFile);
                                //load video on video view
//                                DownloadManager.getInstance(Login.this).enqueue(new DownloadManager.Request(backgroundFile), new DownloadListener() {
//                                    @Override
//                                    public void onProgress(String url, File file, int progress) {
//                                        Log.d(TAG, "cached url ; " + url);
//                                        Log.d(TAG, "cached file ; " + file.getPath());
//                                        Log.d(TAG, "cached progress ; " + progress);
//
////                                        String proxyUrl = MediaLoader.getInstance(Login.this).getProxyUrl(backgroundFile);      //video cache
//                                        loadVideoVideo(url);
//                                    }
//
//                                    @Override
//                                    public void onError(Throwable e) {
//                                        e.printStackTrace();
//                                    }
//                                });
                                String proxyUrl = MediaLoader.getInstance(getApplicationContext()).getProxyUrl(backgroundFile);
                                Log.d(TAG, "proxy url : " + proxyUrl);
                                loadVideoVideo(proxyUrl);
                                if (mediaLoader.isCached(proxyUrl)){
                                    Log.d(TAG, "proxy url is cached : ");
                                }else{
                                    Log.d(TAG, "proxy url is not cached : ");
                                }
                                break;
                            case MODIFIED:
                                //getting the string value for the background file
                                backgroundFile = documentChange.getDocument().getString("file");
                                Log.d(TAG, "login background video file (modified) : " + backgroundFile);
                                //load video on video view
                                DownloadManager.getInstance(Login.this).enqueue(new DownloadManager.Request(backgroundFile), new DownloadListener() {
                                    @Override
                                    public void onProgress(String url, File file, int progress) {
                                        Log.d(TAG, "cached url ; " + url);
                                        Log.d(TAG, "cached file ; " + file.getPath());
                                        Log.d(TAG, "cached progress ; " + progress);

//                                        String proxyUrl = MediaLoader.getInstance(Login.this).getProxyUrl(backgroundFile);      //video cache
                                        loadVideoVideo(url);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                    }
                                });
//                                mediaLoader.addDownloadListener(backgroundFile, new DownloadListener() {
//                                    @Override
//                                    public void onProgress(String url, File file, int progress) {
//                                        Log.d(TAG, "cached url ; " + url);
//                                        Log.d(TAG, "cached file ; " + file.getPath());
//                                        Log.d(TAG, "cached progress ; " + progress);
//
////                                        String proxyUrl = MediaLoader.getInstance(Login.this).getProxyUrl(backgroundFile);      //video cache
//                                        loadVideoVideo(url);
//                                    }
//
//                                    @Override
//                                    public void onError(Throwable e) {
//                                        e.printStackTrace();
//                                    }
//                                });

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

    private void videoCaching() {
        MediaLoaderConfig mediaLoaderConfig = new MediaLoaderConfig.Builder(this)
                .cacheRootDir(DefaultConfigFactory.createCacheRootDir(this))//directory for cached files
                .cacheFileNameGenerator(new HashCodeFileNameCreator())//names for cached files
                .maxCacheFilesCount(100)//max files count
                .maxCacheFilesSize(100 * 1024 * 1024)//max files size
                .maxCacheFileTimeLimit(5 * 24 * 60 * 60)//max file time
                .downloadThreadPoolSize(2)//download thread size
                .downloadThreadPriority(Thread.MAX_PRIORITY)//download thread priority
                .build();
        mediaLoader = MediaLoader.getInstance(this);
        mediaLoader.init(mediaLoaderConfig);
    }

    private void setWindowFullScreen() {
        Window window = getWindow();        //initializing window
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private void isUserLoggedIntoFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            getIntents.goToHome(Login.this);
            Login.this.finish();
        }

    }

    private void initFirebase() {
        //init firestore
        firestoreReference =  FirebaseFirestore.getInstance();
        //init firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
    }


    private void twitterLoginStuff() {
        //twitter login button
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                /*configure and show custom dialog progress*/
                lottieDialogFragment.setCancelable(false);
                Bundle bundle = new Bundle();
                bundle.putString("dialogType", String.valueOf(StringConstants.DialogType.SIGN_IN));
                lottieDialogFragment.setArguments(bundle);
                assert getFragmentManager() != null;
                lottieDialogFragment.show(getSupportFragmentManager(),"");

                Log.d(TAG, "onSuccessTwitterLogin: " + result);
                //saving user to firebase auth
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                String token = result.data.getAuthToken().token;
                String secret = result.data.getAuthToken().secret;
                Log.d(TAG, "Twitter token : " + token);
                Log.d(TAG, "Twitter secret : " + secret);
                AuthCredential authCredential = TwitterAuthProvider.getCredential(token, secret);
                firebaseAuth.signInWithCredential(authCredential)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d(TAG, "onSuccessFirebaseLogin: " + authResult);
                                //checking to see if user is a new user or not so that it doesn't add the user details more than once
                                if (Objects.requireNonNull(authResult.getAdditionalUserInfo()).isNewUser()){
                                    Log.d(TAG, "new user logged in");
                                    if (authResult.getUser() != null){
                                        //creating the document to get the ID and add it as a field in the users collection
                                        DocumentReference documentReference = firestoreReference.collection(StringConstants.USERS_COLLECTION).document();
                                        String documentID = documentReference.getId();

                                        //populating user field
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
                                                        lottieDialogFragment.dismiss();         //dismiss custom dialog
                                                        Login.this.finish();
                                                        Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                }else {
                                    Log.d(TAG, "old user logged in");
                                    //go to home page
                                    getIntents.goToHome(Login.this);
                                    lottieDialogFragment.dismiss();         //dismiss custom dialog
                                    Login.this.finish();
                                    Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        lottieDialogFragment.dismiss();         //dismiss custom dialog
                        Toast.makeText(Login.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailureFirebaseLogin: " + e.getMessage() + "\n caused by : " + e.getCause());
                    }
                });

            }

            @Override
            public void failure(TwitterException exception) {
                //lottieDialogFragment.dismiss();         //dismiss custom dialog {commented this because the dialog fragment starts showing only when the twitter callback is successful hence this will cause app to crash if uncommented}
                Toast.makeText(Login.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorTwitterLogin: " + exception.getMessage() + "\n caused by :" + exception.getCause());
            }
        });
    }

    private void facebookLoginStuff() {
        //facebook login button
        facebookCallbackManager = CallbackManager.Factory.create(); //creating facebook callback
        facebookLoginButton.setPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                /*configure and show custom dialog progress*/
                lottieDialogFragment.setCancelable(false);
                Bundle bundle = new Bundle();
                bundle.putString("dialogType", String.valueOf(StringConstants.DialogType.SIGN_IN));
                lottieDialogFragment.setArguments(bundle);
                assert getFragmentManager() != null;
                lottieDialogFragment.show(getSupportFragmentManager(),"");

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
//                                        String documentID = documentReference.getId();
                                        String documentID = authResult.getUser().getUid();

                                        //population user field
                                        users.setUserName(authResult.getUser().getDisplayName());
                                        users.setUserEmail(authResult.getUser().getEmail());
                                        users.setUserId(documentID);
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
                lottieDialogFragment.dismiss();         //dismiss custom dialog
                Toast.makeText(Login.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCancelFacebookLogin: " + "Log in cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                lottieDialogFragment.dismiss();         //dismiss custom dialog
                Toast.makeText(Login.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorFacebookLogin: " + error.getMessage() + "\n caused by :" + error.getCause());
            }
        });
    }

    private void loadVideoVideo(String backgroundFile) {
        Log.d(TAG, "backgroundVideo " + backgroundFile);
        loginVideoView.setVideoPath(backgroundFile);        //setting video path
        loginVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);       //setting looping to true
                setVolume(0, mediaPlayer);      //mute audio
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

    private void setVolume(int amount, MediaPlayer mediaPlayer) {
        final int max = 100;
        final double numerator = max - amount > 0 ? Math.log(max - amount) : 0;
        final float volume = (float) (1 - (numerator / Math.log(max)));

        mediaPlayer.setVolume(volume, volume);
    }

    private void init() {
        loginVideoView = findViewById(R.id.login_video_view);
        facebookLoginButton = findViewById(R.id.facebook_login_btn);
        twitterLoginButton = findViewById(R.id.twitter_login_btn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*separating twitter and facebook's request and result codes*/
        if (requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE) {
            // Twitter request code
            try {
                //twitter onActivityResult
                twitterLoginButton.onActivityResult(requestCode, resultCode, data);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "error " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()){
            // Use Facebook callback manager here
            try {
                //facebook onActivityResult
                facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "error " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

        }


    }
}
