package com.mcdev.memery;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.picker.gallery.model.GalleryImage;
import com.picker.gallery.model.interactor.GalleryPicker;
import com.picker.gallery.view.PickerActivity;
import com.squareup.picasso.Picasso;

public class AddMemeFromDeviceActivity extends AppCompatActivity {

    private static final String TAG = AddMemeFromDeviceActivity.class.getSimpleName();
    private Uri URI;
    private String PATH;
    private static final int PickMeme = 212;
    ImageView imageView;
    VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meme_from_device);

//        Button clickme = findViewById(R.id.buttonnn);
        imageView = findViewById(R.id.imageViewww);
        videoView = findViewById(R.id.videoViewwww);

        /*Getting string extras*/
        URI = (Uri) getIntent().getExtras().get("URI");
        Log.d(TAG, "URI : " + URI);
        PATH = (String) getIntent().getExtras().get("PATH");
        Log.d(TAG, "PATH : " + PATH);

//        GalleryImage galleryImage = new GalleryImage();
//        String gn = galleryImage.getDISPLAY_NAME();
//        Log.e("TAG", gn);
//        Toast.makeText(this, gn, Toast.LENGTH_SHORT).show();
//        GalleryPicker galleryPicker = new GalleryPicker(this);
//        PickerActivity pickerActivity = new PickerActivity();
//        pickerActivity.getREQUEST_TAKE_PHOTO();
//        galleryPicker.getImages().get(0).get
//        clickme.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*,video/*");
//                startActivityForResult(intent, PickMeme);
//            }
//        });

        if (PATH.contains("/video/")) {
            Log.d(this.getClass().getName(), "Video");
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            loadVideoVideo(URI);
        } else if (PATH.contains("/images/")) {
            Log.d(this.getClass().getName(), "Image");
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(URI).into(imageView);
//                imageView.setImageBitmap();
        }

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
}
