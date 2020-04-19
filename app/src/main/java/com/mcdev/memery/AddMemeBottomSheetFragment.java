package com.mcdev.memery;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddMemeBottomSheetFragment extends BottomSheetDialogFragment {

    FloatingActionButton fab;
    ImageView imageView;
    VideoView videoView;
    private static final int PickMeme = 212;

    public AddMemeBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_meme_bottom_sheet, container, false);

        fab =  view.findViewById(R.id.home_fabb);
        imageView = view.findViewById(R.id.imageViewww);
        videoView = view.findViewById(R.id.videoViewwww);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*,video/*");
                startActivityForResult(intent, PickMeme);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PickMeme && resultCode == RESULT_OK){
            Uri theUri = data.getData();
            Log.e("TAG", theUri.toString());
            Toast.makeText(getContext(), "picked image", Toast.LENGTH_SHORT).show();
            String path = data.getData().getPath();
            if (path.contains("/video/")) {
                Log.d(this.getClass().getName(), "Video");
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                loadVideoVideo(theUri);
            } else if (path.contains("/images/")) {
                Log.d(this.getClass().getName(), "Image");
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Picasso.get().load(theUri).into(imageView);
//                imageView.setImageBitmap();
            }
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
                float scale = Math.min(xScale, yScale);

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
