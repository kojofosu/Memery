package com.mcdev.memery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.andrognito.flashbar.Flashbar;
import com.andrognito.flashbar.anim.FlashAnim;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iammert.library.ui.multisearchviewlib.MultiSearchView;
import com.mcdev.memery.General.StringConstants;
import com.mcdev.memery.POJOS.MemeUploads;
import com.mcdev.memery.databinding.ActivitySearchBinding;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import spencerstudios.com.bungeelib.Bungee;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();

    private FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter adapter;
    private String currentUserID, privacy;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActivitySearchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        //get intent extras
        if (getIntent().getExtras() != null) {
            currentUserID = getIntent().getStringExtra("currentUserID");
            privacy = getIntent().getStringExtra("privacy");
        }

        //init
        init();

        // setup recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        //firebase stuff
        firebaseFirestore = FirebaseFirestore.getInstance();

        //search listener
        searchListener(binding, currentUserID, privacy);

    }

    private void init() {
        recyclerView = findViewById(R.id.search_recyclerview);
    }

    private void searchListener(ActivitySearchBinding binding, String currentUserId, String privacy) {

        binding.multiSearchView.setSearchViewListener(new MultiSearchView.MultiSearchViewListener() {
            @Override
            public void onTextChanged(int i, @NotNull CharSequence charSequence) {

            }

            @Override
            public void onSearchComplete(int i, @NotNull CharSequence charSequence) {
                /*filter memes to display private memes by search*/
                Query query = firebaseFirestore.collection(StringConstants.MEMERIES_COLLECTION)
                        .whereEqualTo("private", true)          //display only private posts
                        .whereEqualTo("uploadedBy", currentUserId)      //display only posts by current user
                        .orderBy("postedAt", Query.Direction.DESCENDING);       //sort recent post on top
                /*pass query to firebase UI*/
                firebaseFirestoreUI(query);
            }

            @Override
            public void onSearchItemRemoved(int i) {

            }

            @Override
            public void onItemSelected(int i, @NotNull CharSequence charSequence) {

            }
        });
    }

    private void firebaseFirestoreUI(Query query) {

        FirestoreRecyclerOptions<MemeUploads> options = new FirestoreRecyclerOptions.Builder<MemeUploads>()
                .setQuery(query, MemeUploads.class)
                .setLifecycleOwner(SearchActivity.this)
                .build();

        adapter = new FirestoreRecyclerAdapter<MemeUploads, SearchActivity.MemeHolder>(options) {

            @NonNull
            @Override
            public MemeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.home_items_layout,parent,false);

                return new SearchActivity.MemeHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MemeHolder holder, int position, @NonNull MemeUploads model) {
                String type = model.getMemeType();
                Log.d(TAG, "type from db : " + type);
                String downloadUrl = model.getDownloadUrl();
                Log.d(TAG, "downloadUrl from db : " + downloadUrl);
                String memeID = model.getMemeId();
                Log.d(TAG, "memeId from db : " + memeID);
                String title = model.getMemeTitle();
                Log.d(TAG, "title from db : " + title);
                long postedAt = model.getPostedAt();
                Log.d(TAG, "postedAt from db : " + postedAt);
                long updatedAt = model.getUpdatedAt();
                Log.d(TAG, "updatedAt from db : " + updatedAt);
                boolean isPrivate = model.isPrivate();
                Log.d(TAG, "isPrivate from db : " + isPrivate);

                if (type.equals("video")){
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.typeTextView.setText(".MP4");
                    long interval = 5000 * 1000;
                    RequestOptions options = new RequestOptions().frame(interval);
                    Glide.with(getApplicationContext()).asBitmap()
                            .load(downloadUrl)
                            .apply(options)
                            .into(holder.imageView);
                }else{
                    holder.imageView.setVisibility(View.VISIBLE);
                    Picasso.get().load(downloadUrl).into(holder.imageView);
                    holder.typeTextView.setText(".JPG");
                }

                //setting title
                holder.titleTextView.setText(title);

                //item onClick
                holderItemClick(holder, position, model);

                //item onLongClick
                holderItemLongClick(holder, position, model);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            /*checking if item count is zero or not*/
            @Override
            public void onDataChanged() {
                super.onDataChanged();

//                /*checking if recycler view is null*/
//                if (Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() == 0) {
//                    Log.d(TAG, "Recycler view is empty with item count " + recyclerView.getAdapter().getItemCount());
//                    /*show no data lottie animation*/
//                    noMemeAnimation.setVisibility(View.VISIBLE);       //make visible
//                    noMemeTV.setVisibility(View.VISIBLE);
//                    recyclerView.setVisibility(View.GONE);      //disappear`
//                } else if (recyclerView.getAdapter().getItemCount() > 0) {
//                    Log.d(TAG, "Recycler view is not empty with item count " + recyclerView.getAdapter().getItemCount());
//                    noMemeAnimation.setVisibility(View.GONE);       //disappear
//                    noMemeTV.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);       //make visible
//                }
            }
        };

        //attaching the adapter to my recycler view
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private class MemeHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        VideoView videoView;
        TextView typeTextView, titleTextView;

        public MemeHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.home_item_card_view);
            imageView = itemView.findViewById(R.id.home_item_image_view);
            videoView = itemView.findViewById(R.id.home_item_video_view);
            typeTextView = itemView.findViewById(R.id.home_item_type_text_view);
            titleTextView = itemView.findViewById(R.id.home_item_title_text_view);
        }
    }

    private void holderItemClick(SearchActivity.MemeHolder holder, int position, MemeUploads model) {
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String documentPath = model.getMemeId();        //getting the meme id which is same as the media name in storage
                String uploadedBy = model.getUploadedBy();          //getting the id of user who posted the meme to avoid unauthorized users from being able to delete it
                String memeTitle = model.getMemeTitle();            //getting the title fo the meme

                long memeDate = model.getPostedAt();            //getting the date the meme was posted
                String memeUrl = model.getDownloadUrl();            //getting the meme's download url
                String memeType = model.getMemeType();          //getting the type of the meme. Either a video, gif or an image
                String getDateInString = getDate(memeDate);

                Intent intent = new Intent(SearchActivity.this, ViewMemeActivity.class);
                intent.putExtra("userId", uploadedBy);
                intent.putExtra("memeId", documentPath);
                intent.putExtra("memeTitle", memeTitle);
                intent.putExtra("memeDate", getDateInString);
                intent.putExtra("memeUrl", memeUrl);
                intent.putExtra("memeType", memeType);
                startActivity(intent);
                Bungee.slideUp(SearchActivity.this);
            }
        });
    }

    private void holderItemLongClick(SearchActivity.MemeHolder holder, int position, MemeUploads model) {
        /*getting current user id from shared preferences*/
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(StringConstants.SHARE_PREF_USER_DETAILS, MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString(StringConstants.SHARE_PREF_USER_ID, null);
        Log.d(TAG, "currentUserID " + currentUserId);
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String documentPath = model.getMemeId();        //getting the meme id which is same as the media name in storage
                String uploadedBy = model.getUploadedBy();          //getting the id of user who posted the meme to avoid unauthorized users from being able to delete it
                String memeTitle = model.getMemeTitle();            //getting the title fo the meme

                long memeDate = model.getPostedAt();            //getting the date the meme was posted
                String memeUrl = model.getDownloadUrl();            //getting the meme's download url
                String memeType = model.getMemeType();          //getting the type of the meme. Either a video, gif or an image
                String getDateInString = getDate(memeDate);

//                MemeDetailsFragment memeDetailsFragment = new MemeDetailsFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString("userId", uploadedBy);
//                bundle.putString("memeId", documentPath);
//                bundle.putString("memeTitle", memeTitle);
//                bundle.putString("memeDate", getDateInString);
//                bundle.putString("memeUrl", memeUrl);
//                bundle.putString("memeType", memeType);
//                memeDetailsFragment.setArguments(bundle);
//                memeDetailsFragment.show(getParentFragmentManager(), "");

                /*checking to see if current user is the one who posted the meme*/
                assert currentUserId != null;
                if (!currentUserId.equals(uploadedBy)){
                    Flashbar progressFlashBar = new Flashbar.Builder(SearchActivity.this)
                            .gravity(Flashbar.Gravity.TOP)
                            .title("Cannot delete.")
                            .message("Post belongs to someone else")
                            .duration(2000L)
                            .showIcon()
                            .icon(R.drawable.ic_warning_24dp)
                            .iconColorFilterRes(R.color.yellow)
                            .iconAnimation(FlashAnim.with(SearchActivity.this).animateIcon()
                                    .pulse()
                                    .alpha()
                                    .duration(750)
                                    .accelerate())
                            .enableSwipeToDismiss()
                            .backgroundDrawable(R.drawable.deleted_bg)
                            .build();
                    progressFlashBar.show();
                }else {
                    //inflating bottom sheet delete confirmation
                    ConfirmationBottomSheetFragment confirmationBottomSheetFragment = new ConfirmationBottomSheetFragment();
                    Bundle bundle = new Bundle();       // init bundle to pass data
                    bundle.putString("confirmationDialogType", String.valueOf(StringConstants.ConfirmationDialog.CONFIRM_DELETE));
                    bundle.putString("currentUserId", currentUserId);
                    bundle.putString("documentPath", documentPath);
                    confirmationBottomSheetFragment.setArguments(bundle);
                    if (getFragmentManager() != null) {
                        confirmationBottomSheetFragment.show(getSupportFragmentManager(), confirmationBottomSheetFragment.getTag());
                    }
                }
                return true;
            }
        });
    }

    private String getDate(long memeDate) {
        Date date = new Date(TimeUnit.SECONDS.toMillis(memeDate));      //accepts the time as long in milliseconds, not seconds. You need to multiply it by 1000 or convert it and make sure that you supply it as long.
        DateFormat simpleDateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
        DateFormat simpleTimeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        return simpleTimeFormat.format(date) + " · " + simpleDateFormat.format(date);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Bungee.slideRight(this);
    }
}