package com.mcdev.memery;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.LayoutMode;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialogKt;
import com.afollestad.materialdialogs.bottomsheets.BottomSheet;
import com.afollestad.materialdialogs.callbacks.DialogCallbackExtKt;
import com.afollestad.materialdialogs.internal.main.DialogLayout;
import com.airbnb.lottie.LottieAnimationView;

import com.andrognito.flashbar.Flashbar;

import com.esafirm.rxdownloader.RxDownloader;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;


/**
 * A simple {@link Fragment} subclass.
 */
public class SaveFragment extends Fragment {
    private final static String TAG = SaveFragment.class.getSimpleName();

    private EditText tweetUrlET;
//    private Button downloadTweetBtn;
    private LottieAnimationView downloadLottieAnimationView;
    private TextView progressTextView;
    Flashbar progressFlashBar;

    public SaveFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_save, container, false);

        //init
        init(view);

        if (getArguments() != null){
            String getURLFromActivity = getArguments().getString("tweetURL");
            if (getURLFromActivity != null) {
                Log.println(Log.ASSERT,"getURLFromActivity", getURLFromActivity);
            }

            tweetUrlET.setText(getURLFromActivity);
        }




        //listeners
        downloadLottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Fetching...", Toast.LENGTH_SHORT).show();     //Toast to the user
                progressTextView.setText("Fetching...");
                Long id = getTweetId(tweetUrlET.getText().toString());      //getting tweet id from edit text and passing to getTweetId func
                Log.d("TAG", "tweet is + " + tweetUrlET.getText().toString());
                if (id !=null) {        //checking if string id is not null
                    Log.d("TAG", "tweet id is + " + id);
                    getTweet(id);       //getTweet
                }
            }
        });
        return view;
    }
    /*Get tweet*/
    private void getTweet(final Long id) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();       //initializing twitter api client
        StatusesService statusesService = twitterApiClient.getStatusesService();        //getting twitter status service
        Call<Tweet> tweetCall = statusesService.show(id,null, null, null);      //passing tweet to retrofit call
        tweetCall.enqueue(new Callback<Tweet>() {       //retrofit enqueue
            @Override
            public void success(Result<Tweet> result) {
                int i=0;
                downloadLottieAnimationView.playAnimation();        //play lottie animation when tweet details return success for clearer animation
                String inReplyToScreenName = result.data.inReplyToScreenName;      //If the represented Tweet is a reply, this field will contain the screen name of the original Tweet's author.
                String createdAt = result.data.createdAt;        //The time the tweet was created
                String tweetUID = result.data.idStr;        //The tweet's unique identifier string representation
                String inReplyToTweetUID = result.data.inReplyToStatusIdStr;         // If the represented Tweet is a reply, this field will contain the string representation of the original Tweet's ID
                String inReplyToUserUID = result.data.inReplyToUserIdStr;           //If the represented Tweet is a reply, this field will contain the string representation of the original Tweet's author ID. This will not necessarily always be the user directly mentioned in the Tweet
                String quotedTweetUID = result.data.quotedStatusIdStr;        //This field only surfaces when the Tweet is a quote Tweet. This is the string representation Tweet ID of the quoted Tweet
                String twitterFor = result.data.source;     //Utility used to post the Tweet, as an HTML-formatted string. Tweets from the Twitter website have a source value of web
                String tweetText = result.data.text;        //The actual UTF-8 text of the status update. See twitter-text for details on what is currently considered valid characters.
                String username = result.data.user.name;        //The name of the user, as they've defined it. Not necessarily a person's name. Typically capped at 20 characters, but subject to change
                String userScreenName = result.data.user.screenName;        //The screen name, handle, or alias that this user identifies themselves with. screen_names are unique but subject to change. Use id_str as a user identifier whenever possible. Typically a maximum of 15 characters long, but some historical accounts may exist with longer names.
                String userUID = result.data.user.idStr;           //The string representation of the unique identifier for this User. Implementations should use this rather than the large, possibly un-consumable integer in id
//                String hashTags = result.data.entities.hashtags.get(i).text;       // Name of the hashtag, minus the leading '#' character.
                String tweetType = result.data.extendedEntities.media.get(0).type;      //The tweet type


//                String url;     //file url
                Log.d("TAG", "tweet type is : " + result.data.extendedEntities.media.get(0).type);

                /*Checking if file is video or an animated gif*/
                if ((tweetType).equals("video")) {
                    String filename = "memery_vid" + id + ".mp4";      //set file name
                    String mimeType = "video/*";
                    String url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(i).url;
                    Log.d("TAG", "url is " + url);
                    final String getUrl = url;
                    final String getfileName = filename;
                    final String getMimeType = mimeType;
                    downloadLottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            /*Below codes didn't get called for reasons i do not know*/
                            //Toast.makeText(getContext(), "Fetching...", Toast.LENGTH_SHORT).show();
                            //progressTextView.setText("Fetching...");
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            progressTextView.setText("");
                            progressFlashBar = new Flashbar.Builder(getActivity())
                                    .gravity(Flashbar.Gravity.TOP)
                                    .title("Downloading...")
                                    .message(getfileName)
                                    .showProgress(Flashbar.ProgressPosition.LEFT)
                                    //.duration(Flashbar.DURATION_INDEFINITE)       //commented this because it will crash...to make duration indefinite, don't call duration()
                                    .enableSwipeToDismiss()
                                    .backgroundDrawable(R.drawable.flash_bar_gradient)
                                    .build();
                            progressFlashBar.show();
                            downloadVideo(getUrl,getfileName, getMimeType);        //download video
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                }else if ((tweetType).equals("animated_gif")){
                    String filename = "memery_gif" + id + ".mp4";      //set file name with extension ".mp4" because setting it to .gif gave me issues and also because the url file was .mp4
                        String url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(i).url;
                        Log.d("TAG", "url is " + url);

                    while (url.endsWith(".gif")){
                        if(result.data.extendedEntities.media.get(0).videoInfo.variants.get(i)!=null) {
                            url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(i).url;
                            i += 1;
                        }
                    }

                    String mimeType = "image/gif";    //mime type for gifs is "image/gif"

                    final String getUrl = url;
                    final String getfileName = filename;
                    final String getMimeType = mimeType;
                    downloadLottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            /*Below codes didn't get called for reasons i do not know*/
                            //Toast.makeText(getContext(), "Fetching...", Toast.LENGTH_SHORT).show();
                            //progressTextView.setText("Fetching...");
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            progressTextView.setText("");
                            progressFlashBar = new Flashbar.Builder(getActivity())
                                    .gravity(Flashbar.Gravity.TOP)
                                    .title("Downloading...")
                                    .message(getfileName)
                                    .showProgress(Flashbar.ProgressPosition.LEFT)
                                    //.duration(Flashbar.DURATION_INDEFINITE)       //commented this because it will crash...to make duration indefinite, don't call duration()
                                    .enableSwipeToDismiss()
                                    .backgroundDrawable(R.drawable.flash_bar_gradient)
                                    .build();
                            progressFlashBar.show();
                            downloadVideo(getUrl,getfileName, getMimeType);        //download video

                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                }


            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getContext(), "Couldn't fetch tweet.", Toast.LENGTH_SHORT).show();     //Toast to the user
                progressTextView.setText("Couldn't fetch tweet.");
            }
        });
    }

    /*download video*/
    private void downloadVideo(String url, final String filename, @Nullable String mimeType) {
        File dir = new File(Environment.DIRECTORY_DOWNLOADS + "/Memeries");     //creating memeries custom directory
        if (!dir.exists()) {
            dir.mkdirs();       // creates needed dirs
        }
        String downloadDestination = String.valueOf(dir);       //getting the string equivalent of the path to be passed to rxDownloader
        Log.d("TAG", "downloadDestination : " + downloadDestination);



        final RxDownloader rxDownloader = new RxDownloader(getContext());     //init RxDownloader
        rxDownloader.download(url, filename, downloadDestination, mimeType, true)
        .subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) { ;
                Log.d("TAG", "subscribe onSubscribe " + d.isDisposed());        //returns true if subscriber is disposed
            }

            @Override
            public void onNext(String s) {
                Log.d("TAG", "subscribe onNext " + s);      //This returns the file path of the downloaded file
            }

            @Override
            public void onError(Throwable e) {
                Log.d("TAG", "subscribe onError " + e.getLocalizedMessage());       //get error message
            }

            @Override
            public void onComplete() {
                Log.d("TAG", "subscribe onComplete " );         //download complete
                tweetUrlET.setText("");     //clearing the url from the edit text when download is complete
                progressFlashBar.dismiss();
                Flashbar flashbar = new Flashbar.Builder(getActivity())
                        .gravity(Flashbar.Gravity.TOP)
                        .message("Download complete")
                        .duration(5000)
                        .enableSwipeToDismiss()
                        .backgroundDrawable(R.drawable.flash_bar_gradient)
                        .vibrateOn(Flashbar.Vibration.SHOW, Flashbar.Vibration.DISMISS)
                        .build();
                flashbar.show();

                //timer to refresh page to unregister from the downloader {This is temporary}
                final int interval = 6000;      // 1 Second before the item enables again for user to be able to click
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        Intent intent = getActivity().getIntent();
                        getActivity().overridePendingTransition(0, 0);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        getActivity().finish();
                        getActivity().overridePendingTransition(0, 0);
                        startActivity(intent);
                    }
                };
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);        //chip enabler handler
                handler.postDelayed(runnable, interval);
            }
        });


    }

    private void init(@NotNull View view ) {
        tweetUrlET = view.findViewById(R.id.tweetD_url);
//        downloadTweetBtn = view.findViewById(R.id.downloadTweet);
        downloadLottieAnimationView = view.findViewById(R.id.downloadTweet);
        progressTextView = view.findViewById(R.id.progress_textview);
    }

    @Nullable
    private static Long getTweetId(String s) {
        try {
            String[] split = s.split("\\/");
            String id = split[5].split("\\?")[0];
            return Long.parseLong(id);
        }catch (Exception e){
            Log.d("TAG", "getTweetId: " + e.getLocalizedMessage());
//            alertNoUrl();
            return null;
        }
    }
}
