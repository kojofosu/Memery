package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mcdev.memery.General.StringConstants;
import com.squareup.picasso.Picasso;

import render.animations.Attention;
import render.animations.Render;

public class AddMemeFromDeviceActivity extends AppCompatActivity {

    private static final String TAG = AddMemeFromDeviceActivity.class.getSimpleName();
    private Uri URI;
    private String PATH, MIME_TYPE;
    ImageView imageView;
    VideoView videoView;
    private FloatingActionButton fab;
    private EditText memeCaptionET;
    private LottieAnimationView lottieAnimationView;
    private TextView privateTextView;
    private LinearLayout privateLinearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meme_from_device);

        //init
        init();

        //set fullscreen
        setWindowFullScreen();

        //getting data from share
        Intent shareIntent = getIntent();
        String action = shareIntent.getAction();
        String type = shareIntent.getType();

        String selectedType = null;        //to get the type of content that was selected either an image or video file


        privateLinearLayout.setSelected(true);      //setting this to true to make below code work properly
        privateLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set animation
                customAnimateView(AddMemeFromDeviceActivity.this, privateTextView);

                if (privateLinearLayout.isSelected()){
                    lottieAnimationView.setSpeed(1);
                    lottieAnimationView.setFrame(45);
                    lottieAnimationView.setMaxFrame(60);
                    lottieAnimationView.resumeAnimation();

                    privateTextView.setText(R.string.private_post);

                    privateLinearLayout.setSelected(false);         //needed
                }else{
                    lottieAnimationView.setSpeed(-1);
                    lottieAnimationView.setMinFrame(45);
                    lottieAnimationView.resumeAnimation();
//                lottieAnimationView.resumeAnimation();
//                lottieAnimationView.setMaxFrame(60);

                    privateTextView.setText(R.string.public_post);
                    privateLinearLayout.setSelected(true);      //needed
                }
            }

            private void customAnimateView(AddMemeFromDeviceActivity addMemeFromDeviceActivity, TextView privateTextView) {
                // Create Render Class
                Render render = new Render(addMemeFromDeviceActivity);
                render.setAnimation(Attention.Shake(privateTextView));
                render.start();
            }
        });

        /*separated the URIs because getting the uri from shared in tent and getting uri from in app take different paths*/
        if ("android.intent.action.SEND".equals(action)) {
            URI = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (type != null) {

                if (type.startsWith("image/")) {
                    if (URI != null) {
                        Log.println(Log.ASSERT, "shareableUriExtra", String.valueOf(URI));
                        videoView.setVisibility(View.GONE);         //setting video view invisible
                        imageView.setVisibility(View.VISIBLE);      //setting image view visible
                        selectedType = "image/gif";     //image or gif
                        Picasso.get().load(URI).into(imageView);        //load image with picasso
                    }
                    Log.d(TAG, "action : " + action);
                    Log.d(TAG, "type : " + type);
                    Log.d(TAG, "uri : " + URI);
                } else if (type.startsWith("video/")) {
                    if (URI != null) {
                        Log.println(Log.ASSERT, "shareableUriExtra", String.valueOf(URI));
                        Log.d(this.getClass().getName(), "Video");
                        imageView.setVisibility(View.GONE);             //setting image view invisible
                        videoView.setVisibility(View.VISIBLE);      //setting video view visible
                        selectedType = "video";
                        loadVideoVideo(URI);        //load video
                    }
                    Log.d(TAG, "action : " + action);
                    Log.d(TAG, "type : " + type);
                    Log.d(TAG, "uri : " + URI);
                }


            }
        }else {
            URI = shareIntent.getData();
            if (type != null) {

                if (type.startsWith("image/")) {
                    if (URI != null) {
                        Log.println(Log.ASSERT, "shareableUriExtra", String.valueOf(URI));
                        videoView.setVisibility(View.GONE);         //setting video view invisible
                        imageView.setVisibility(View.VISIBLE);      //setting image view visible
                        selectedType = "image/gif";     //image or gif
                        Picasso.get().load(URI).into(imageView);        //load image with picasso
                    }
                    Log.d(TAG, "action : " + action);
                    Log.d(TAG, "type : " + type);
                    Log.d(TAG, "uri : " + URI);
                } else if (type.startsWith("video/")) {
                    if (URI != null) {
                        Log.println(Log.ASSERT, "shareableUriExtra", String.valueOf(URI));
                        Log.d(this.getClass().getName(), "Video");
                        imageView.setVisibility(View.GONE);             //setting image view invisible
                        videoView.setVisibility(View.VISIBLE);      //setting video view visible
                        selectedType = "video";
                        loadVideoVideo(URI);        //load video
                    }
                    Log.d(TAG, "action : " + action);
                    Log.d(TAG, "type : " + type);
                    Log.d(TAG, "uri : " + URI);
                }


            }

        }

        /*this code needed to come below the above codes to be able to set selectedType to a value and not render it null*/
        //listeners
        if (selectedType != null){
            Log.d(TAG, "meme type " + selectedType);
            fabListener(selectedType);      //handle fab listener when the selected item is not null
        }else{
            Log.e(TAG, "No content selected");
        }

    }

    private void fabListener(String selectedType) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //first posting the meme to firebase storage to get the download url
                String caption = memeCaptionET.getText().toString().trim();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(StringConstants.SHARE_PREF_USER_DETAILS, MODE_PRIVATE);
                String eyeDee = sharedPreferences.getString(StringConstants.SHARE_PREF_USER_ID, null);
                Log.d(TAG, "userID " + eyeDee);
                Log.d(TAG, "Uri for storage " + URI);
                if (eyeDee != null){
                    postMemeToStorage(eyeDee, URI, caption, selectedType);      //post meme to storage
                }


            }
        });
    }

    private void postMemeToStorage(String currentUserId, Uri URI, String caption, String selectedType) {
        /*init custom lottie dialog, pass parameters and inflate view*/
        LottieDialogFragment lottieDialogFragment = new LottieDialogFragment();
        lottieDialogFragment.setCancelable(false);
        Bundle bundle = new Bundle();
        bundle.putString("dialogType", String.valueOf(StringConstants.DialogType.UPLOAD_FILES));
        bundle.putString("URI", String.valueOf(URI));
        bundle.putString("currentUserId", currentUserId);
        bundle.putString("caption", caption);
        bundle.putString("selectedType",selectedType);
        /*checking to see if the current post is supposed to be private or public*/
        if (privateTextView.getText().equals(StringConstants.PRIVATE_POST)){
            //make post private
            bundle.putBoolean("isPrivate", true);
        } else if (privateTextView.getText().equals(StringConstants.PUBLIC_POST)) {
            //make post public
            bundle.putBoolean("isPrivate", false);
        }
        lottieDialogFragment.setArguments(bundle);
        lottieDialogFragment.show(getSupportFragmentManager(),"");


    }

    private void init() {
        imageView = findViewById(R.id.imageViewww);
        videoView = findViewById(R.id.videoViewwww);
        fab = findViewById(R.id.post_meme_fab);
        memeCaptionET = findViewById(R.id.meme_caption);
        privateLinearLayout = findViewById(R.id.set_private_linearLayout);
        privateTextView = findViewById(R.id.set_private_textView);
        lottieAnimationView = findViewById(R.id.set_private_lottieView);
    }


    private void loadVideoVideo(Uri backgroundFile) {
        Log.d("TAG", "backgroundVideo " + backgroundFile);
        videoView.setVideoURI(backgroundFile);        //setting video path
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);       //setting looping to true
                setVolume(100, mediaPlayer);      //mute audio
                //Get your video's width and height
                int videoWidth = mediaPlayer.getVideoWidth();
                int videoHeight = mediaPlayer.getVideoHeight();

                //Get VideoView's current width and height
                int videoViewWidth = videoView.getWidth();
                int videoViewHeight = videoView.getHeight();

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
                ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
                layoutParams.width = (int)scaledWidth;
                layoutParams.height = (int)scaledHeight;
                videoView.setLayoutParams(layoutParams);
            }
        });
        videoView.start(); //start playing video
    }

    private void setVolume(int amount, MediaPlayer mediaPlayer) {
        final int max = 100;
        final double numerator = max - amount > 0 ? Math.log(max - amount) : 0;
        final float volume = (float) (1 - (numerator / Math.log(max)));

        mediaPlayer.setVolume(volume, volume);
    }

    private void setWindowFullScreen() {
        Window w = getWindow();     //initializing window
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);     //setting window flags to make status bar translucent
    }

}
