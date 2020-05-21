package com.mcdev.memery;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Objects;

import spencerstudios.com.bungeelib.Bungee;

public class ViewMemeActivity extends AppCompatActivity {
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private PlayerView playerView;

    SimpleExoPlayer player;
    ImageView imageView;
    Uri uri;
    String memeType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_view_meme);

        //init
        init();

        //get intents
        uri = Uri.parse(Objects.requireNonNull(getIntent().getExtras()).getString("memeUrl"));
        memeType = getIntent().getExtras().getString("memeType");

        //player view
//        initializePlayer(uri);
    }


    private void initializeImageView(Uri uri) {

        Glide
                .with(this)
                .load(uri)
                .into(imageView);
    }

    private void initializePlayer(Uri uri) {
        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext());       //instantiating player
        playerView.setPlayer(player);
        player.setPlayWhenReady(true);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.seekTo(currentWindow, playbackPosition);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource);
    }

    private void init() {
        playerView = findViewById(R.id.player_view);
        imageView = findViewById(R.id.view_meme_image_view);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "exoplayer-sample");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bungee.slideDown(ViewMemeActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            if (memeType.equals("video")) {
                playerView.setVisibility(View.VISIBLE);
                initializePlayer(uri);
            } else {
                imageView.setVisibility(View.VISIBLE);
                initializeImageView(uri);
            }
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            if (memeType.equals("video")) {
                playerView.setVisibility(View.VISIBLE);
                initializePlayer(uri);
            } else {
                imageView.setVisibility(View.VISIBLE);
                initializeImageView(uri);
            }
        }
    }



    /*hideSystemUi is a helper method called in onResume which allows us to have a full screen experience. */
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

}
