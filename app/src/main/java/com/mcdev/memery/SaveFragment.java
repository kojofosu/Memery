package com.mcdev.memery;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import com.andrognito.flashbar.Flashbar;

import com.esafirm.rxdownloader.RxDownloader;
import com.github.kotvertolet.youtubejextractor.YoutubeJExtractor;
import com.github.kotvertolet.youtubejextractor.exception.ExtractionException;
import com.github.kotvertolet.youtubejextractor.exception.YoutubeRequestException;
import com.github.kotvertolet.youtubejextractor.models.youtube.videoData.YoutubeVideoData;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;


/**
 * A simple {@link Fragment} subclass.
 */
public class SaveFragment extends Fragment {
    private final static String TAG = SaveFragment.class.getSimpleName();

    private EditText tweetUrlET;
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
        downloadLottieAnimationView.setEnabled(true);       //enabling download button
        downloadLottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if user has granted permissions
                /*DEXTER RUNTIME PERMISSIONS*/
                checkPermissionsWithDexter();
            }
        });
        return view;
    }

    private void checkPermissionsWithDexter() {
        Dexter.withContext(getContext())
                .withPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission granted");
                        goAheadWithDownload();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission denied");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();        //needed for dialog to show a second time when user denies it
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError dexterError) {
                        Log.e("Dexter", "There was an error: " + dexterError.toString());
                    }
                })
                .check();
    }

    private void goAheadWithDownload() {
        Toast.makeText(getContext(), "Fetching...", Toast.LENGTH_SHORT).show();     //Toast to the user
        progressTextView.setText(R.string.fetching);
        String LinkURL = tweetUrlET.getText().toString();
        //checking to see if content is Twitter of Youtube
        if(LinkURL.contains("twitter.com")){
            Log.d(TAG, "This is a twitter content ");
            Long id = getTweetId(LinkURL);      //getting tweet id from edit text and passing to getTweetId func
            Log.d("TAG", "tweet is + " + tweetUrlET.getText().toString());
            if (id !=null) {        //checking if string id is not null
                Log.d("TAG", "tweet id is + " + id);
                getTweet(id);       //getTweet
            }
        }
        else if (LinkURL.contains("youtube.com") || LinkURL.contains("youtu.be")){
            new AsyncCaller().execute(LinkURL);
//            new YTExtractor(getContext()){
//                @Override
//                protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
//                    if (ytFiles != null) {
//                        int itag = 22;
//                        String downloadUrl = ytFiles.get(itag).getUrl();
//                        String author = videoMeta.getAuthor();
//                        String title = videoMeta.getTitle();
//                        Log.d(TAG, "downloadUrl : " + downloadUrl);
//                        Log.d(TAG, "author : " + author);
//                        Log.d(TAG, "title : " + title);
//
//                        String filename = author + title + ".mp4";
//                        Log.d(TAG, "filename : " + filename);
//                        String mimeType = "video/*";
//                        //download youtube video
////                        downloadVideo(downloadUrl, filename, mimeType);
//                    }
//                }
//            }.extract(LinkURL, true, true);


//            new YouTubeExtractor(getContext()) {
//                @Override
//                protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
//                    if (ytFiles != null) {
//                        int itag = 22;
//                        String downloadUrl = ytFiles.get(itag).getUrl();
//                        String author = videoMeta.getAuthor();
//                        String title = videoMeta.getTitle();
//                        Log.d(TAG, "downloadUrl : " + downloadUrl);
//                        Log.d(TAG, "author : " + author);
//                        Log.d(TAG, "title : " + title);
//
//                        String filename = author + title + ".mp4";
//                        Log.d(TAG, "filename : " + filename);
//                        String mimeType = "video/*";
//                        //download youtube video
////                        downloadVideo(downloadUrl, filename, mimeType);
//                    }
//                }
//            }.extract(LinkURL, true, true);

        }

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
                //String hashTags = result.data.entities.hashtags.get(i).text;       // Name of the hashtag, minus the leading '#' character.
                String tweetType = result.data.extendedEntities.media.get(0).type;      //The tweet type


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
                                    .backgroundDrawable(R.drawable.twitterbg)
                                    .build();
                            progressFlashBar.show();
                            downloadTwitterVideo(getUrl,getfileName, getMimeType);        //download video
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
                            progressTextView.setText("");       //vanishing the fetching text
                            downloadLottieAnimationView.setEnabled(false);      //disabling the download button when there's alread a download in progress

                            progressFlashBar = new Flashbar.Builder(getActivity())
                                    .gravity(Flashbar.Gravity.TOP)
                                    .title("Downloading...")
                                    .message(getfileName)
                                    .showProgress(Flashbar.ProgressPosition.LEFT)
                                    //.duration(Flashbar.DURATION_INDEFINITE)       //commented this because it will crash...to make duration indefinite, don't call duration()
                                    .enableSwipeToDismiss()
                                    .backgroundDrawable(R.drawable.twitterbg)
                                    .build();
                            progressFlashBar.show();
                            downloadTwitterVideo(getUrl,getfileName, getMimeType);        //download video

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

    /*download twitter video*/
    private void downloadTwitterVideo(String url, final String filename, @Nullable String mimeType) {
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
                downloadLottieAnimationView.setEnabled(true);       //enabling the download button
                progressFlashBar.dismiss();
                Flashbar flashbar = new Flashbar.Builder(getActivity())
                        .gravity(Flashbar.Gravity.TOP)
                        .message("Download complete")
                        .duration(5000)
                        .enableSwipeToDismiss()
                        .backgroundDrawable(R.drawable.twitterbg)
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
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

    /*download youtube video*/
    private void downloadYoutubeVideo(String url, final String filename, @Nullable String mimeType) {
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
                        downloadLottieAnimationView.setEnabled(true);       //enabling the download button
                        progressFlashBar.dismiss();
                        Flashbar flashbar = new Flashbar.Builder(getActivity())
                                .gravity(Flashbar.Gravity.TOP)
                                .message("Download complete")
                                .duration(5000)
                                .enableSwipeToDismiss()
                                .backgroundDrawable(R.drawable.youtubebg)
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
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

    private String getYouTubeId (String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if(matcher.find()){
            return matcher.group();
        } else {
            return "error";
        }
    }

    private class AsyncCaller extends AsyncTask<String, Void, Void>
    {
        String muxedUrl;
        String filename;

        @Override
        protected Void doInBackground(String... strings) {

            String url = strings[0];
            Log.d(TAG, "This is a youtube content ");
            Log.d(TAG, "youtube url is : " + url);
            String id = getYouTubeId(url);
            Log.d(TAG, "Youtube video ID : " + id);
            YoutubeJExtractor youtubeJExtractor = new YoutubeJExtractor();
            YoutubeVideoData youtubeVideoData;
            try {
                youtubeVideoData = youtubeJExtractor.extract(id);
                String title = youtubeVideoData.getVideoDetails().getTitle();
                String author = youtubeVideoData.getVideoDetails().getAuthor();
                filename =  author + " " +title;
                muxedUrl = youtubeVideoData.getStreamingData().getMuxedStreams().get(0).getUrl();
                Log.d(TAG, "filename : " + filename );
                Log.d(TAG, "author : " + author);
                Log.d(TAG, "title : " + title);
                Log.d(TAG, "probe url : " + youtubeVideoData.getStreamingData().getProbeUrl());
                Log.d(TAG, "dash manifest url : " + youtubeVideoData.getStreamingData().getDashManifestUrl());
                Log.d(TAG, "hls manifest url : " + youtubeVideoData.getStreamingData().getHlsManifestUrl());
                Log.d(TAG, "muxed url : " + youtubeVideoData.getStreamingData().getMuxedStreams().get(0).getUrl());

            } catch (ExtractionException e) {
                e.printStackTrace();
            } catch (YoutubeRequestException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            filename = escapeMetaCharacters(filename) + ".mp4";
            Log.d(TAG, "escapeMetaCharacters : " + filename);
            downloadLottieAnimationView.playAnimation();        //play lottie animation when tweet details return success for clearer animation
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
                                    .message(filename)
                                    .showProgress(Flashbar.ProgressPosition.LEFT)
                                    //.duration(Flashbar.DURATION_INDEFINITE)       //commented this because it will crash...to make duration indefinite, don't call duration()
                                    .enableSwipeToDismiss()
                                    .backgroundDrawable(R.drawable.youtubebg)
                                    .build();
                            progressFlashBar.show();
                            downloadYoutubeVideo(muxedUrl, filename, "video/*");;        //download video
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

    public String escapeMetaCharacters(String inputString){
        final String[] metaCharacters = {"\\","/","^","$","{","}","[","]","(",")",".","*","+","?","|","<",">","-","&","%"};

        for (int i = 0 ; i < metaCharacters.length ; i++){
            if(inputString.contains(metaCharacters[i])){
//                inputString = inputString.replace(metaCharacters[i],"\\"+metaCharacters[i]);
                inputString = inputString.replace(metaCharacters[i],"");
            }
        }
        return inputString;
    }
}
