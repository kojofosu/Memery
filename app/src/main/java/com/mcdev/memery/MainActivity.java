package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mcdev.memery.General.StringConstants;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    LottieAnimationView loginLottieAnimationView;
    VideoView loginVideoView;
    FirebaseFirestore loginBackgroundFirestoreReference;
    private String backgroundFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        init();

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
    }
}
