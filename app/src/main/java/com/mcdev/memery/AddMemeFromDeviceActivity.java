package com.mcdev.memery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mcdev.memery.General.StringConstants;
import com.mcdev.memery.POJOS.MemeUploads;
import com.squareup.picasso.Picasso;

public class AddMemeFromDeviceActivity extends AppCompatActivity {

    private static final String TAG = AddMemeFromDeviceActivity.class.getSimpleName();
    private Uri URI;
    private String PATH;
    ImageView imageView;
    VideoView videoView;
    private FloatingActionButton fab;
    private EditText memeCaptionET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meme_from_device);

        //init
        init();

        //set fullscreen
        setWindowFullScreen();

        /*Getting string extras*/
        URI = (Uri) getIntent().getExtras().get("URI");
        Log.d(TAG, "URI : " + URI);
        PATH = (String) getIntent().getExtras().get("PATH");
        Log.d(TAG, "PATH : " + PATH);

        String selectedType = null;        //to get the type of content that was selected either an image or video file

        if (PATH.contains("/video/")) {
            Log.d(this.getClass().getName(), "Video");
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            selectedType = "video";
            loadVideoVideo(URI);
        } else if (PATH.contains("/images/")) {
            Log.d(this.getClass().getName(), "Image");
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            selectedType = "image/gif";
            Picasso.get().load(URI).into(imageView);
        }

        //listeners
        if (selectedType != null){
            Log.d(TAG, "meme type " + selectedType);
            fabListener(selectedType);
        }else{
            Log.e(TAG, "No content selected");
        }



    }

    private void fabListener(String selectedType) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //first posting the meme to firebase storage to get the download url
                String caption = memeCaptionET.getText().toString();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserDetails", MODE_PRIVATE);
                String eyeDee = sharedPreferences.getString("userID", null);
                Log.d(TAG, "userID " + eyeDee);
                Log.d(TAG, "Uri for storage " + URI);
                if (eyeDee != null){
                    postMemeToStorage(eyeDee, URI, caption, selectedType);
                }


            }
        });
    }

    private void postMemeToStorage(String currentUserId, Uri URI, String caption, String selectedType) {
        //database
        FirebaseFirestore firebaseFirestore =  FirebaseFirestore.getInstance();     //init firestore
        DocumentReference documentReference = firebaseFirestore.collection(StringConstants.MEMERIES_COLLECTION).document();     //init document
        String memeID = documentReference.getId();          //generation meme id and storing it in String variable
        //storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference memeStorageRef = storageReference.child(StringConstants.STORAGE_MEME_UPLOADS).child(currentUserId).child(memeID);      //meme id was the last child so as to prevent the overriding of uploads
        memeStorageRef.putFile(URI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //getting download url of meme
                memeStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Getting current timestamp
                        Long tsLong = System.currentTimeMillis()/1000;

                        //populating meme upload field
                        MemeUploads memeUploads = new MemeUploads();
                        memeUploads.setUploadedBy(currentUserId);
                        memeUploads.setMemeId(memeID);
                        memeUploads.setMemeTitle(caption);
                        memeUploads.setMemeType(selectedType);
                        memeUploads.setPostedAt(tsLong);
                        memeUploads.setDownloadUrl(uri.toString());
                        memeUploads.setPrivate(false);

                        //posting to db
                        firebaseFirestore.collection(StringConstants.MEMERIES_COLLECTION)
                                .document(memeID)
                                .set(memeUploads)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Upload Success");
                                        Toast.makeText(AddMemeFromDeviceActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Uploading meme failed with " + e.getLocalizedMessage());
                Toast.makeText(AddMemeFromDeviceActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        imageView = findViewById(R.id.imageViewww);
        videoView = findViewById(R.id.videoViewwww);
        fab = findViewById(R.id.post_meme_fab);
        memeCaptionET = findViewById(R.id.meme_caption);
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
