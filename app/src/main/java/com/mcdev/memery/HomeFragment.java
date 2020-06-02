package com.mcdev.memery;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.andrognito.flashbar.Flashbar;
import com.andrognito.flashbar.anim.FlashAnim;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mcdev.memery.General.StringConstants;
import com.mcdev.memery.POJOS.MemeUploads;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import render.animations.Attention;
import render.animations.Render;
import spencerstudios.com.bungeelib.Bungee;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

public class HomeFragment extends Fragment {

    private FloatingActionButton homeFAB;
    private static final int PickMeme = 212;
    private static final String TAG = HomeFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private LinearLayout togglePrivateLayout;
    private TextView togglePrivateTV;
    private FirebaseFirestore firebaseFirestore;
    private SharedPreferences sharedPreferences;
    private String currentUserId;
    FirestoreRecyclerAdapter adapter;

    LottieAnimationView noMemeAnimation;
    TextView noMemeTV;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init
        init(view);

        //shared prefs
        sharedPreferences = requireContext().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("userID", "");

        //listeners
        fabListener();

        //firebase stuff
        firebaseFirestore = FirebaseFirestore.getInstance();

        //filter memes by type at launch
        filterMemes();


        //toggle private listener
        togglePrivateListener();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);


        return view;
    }

    private void togglePrivateListener() {
        togglePrivateLayout.setSelected(true);      //setting this to true to make below code work properly
        togglePrivateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set animation
                customAnimateView(getApplicationContext(), togglePrivateTV);


                if (togglePrivateLayout.isSelected()){
                    togglePrivateTV.setText(R.string.private_post);
                    filterMemes();          //filter meme results
                    togglePrivateLayout.setSelected(false);         //needed
                }else{
                    togglePrivateTV.setText(R.string.public_post);
                    filterMemes();          //filter meme results
                    togglePrivateLayout.setSelected(true);      //needed
                }

            }
            private void customAnimateView(Context context, TextView privateTextView) {
                // Create Render Class
                Render render = new Render(context);
                render.setAnimation(Attention.Shake(privateTextView));
                render.start();
            }
        });
    }

    private void filterMemes() {
        if (togglePrivateTV.getText().equals(StringConstants.PRIVATE_POST)){
            /*filter memes to display private memes only by the current user*/
            Query query = firebaseFirestore.collection(StringConstants.MEMERIES_COLLECTION)
                    .whereEqualTo("private", true)          //display only private posts
                    .whereEqualTo("uploadedBy", currentUserId)      //display only posts by current user
                    .orderBy("postedAt", Query.Direction.DESCENDING);       //sort recent post on top
            /*pass query to firebase UI*/
            firebaseFirestoreUI(query);
        } else if (togglePrivateTV.getText().equals(StringConstants.PUBLIC_POST)) {
            //filter post public
            Query query = firebaseFirestore
                    .collection(StringConstants.MEMERIES_COLLECTION)
                    .whereEqualTo("private", false)         //display only public posts
                    .orderBy("postedAt", Query.Direction.DESCENDING);       //sort recent posts on top
            /*pass query to firebase UI*/
            firebaseFirestoreUI(query);
        }
    }

    private void firebaseFirestoreUI(Query query) {

        FirestoreRecyclerOptions<MemeUploads> options = new FirestoreRecyclerOptions.Builder<MemeUploads>()
                .setQuery(query, MemeUploads.class)
                .setLifecycleOwner(getParentFragment())
                .build();

        adapter = new FirestoreRecyclerAdapter<MemeUploads, MemeHolder>(options) {




            @NonNull
            @Override
            public MemeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.home_items_layout,parent,false);

                return new MemeHolder(view);
            }

            /*getting item count*/
            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            /*checking if item count is zero or not*/
            @Override
            public void onDataChanged() {
                super.onDataChanged();

                /*checking if recycler view is null*/
                if (Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() == 0) {
                    Log.d(TAG, "Recycler view is empty with item count " + recyclerView.getAdapter().getItemCount());
                    /*show no data lottie animation*/
                    noMemeAnimation.setVisibility(View.VISIBLE);       //make visible
                    noMemeTV.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);      //disappear`
                } else if (recyclerView.getAdapter().getItemCount() > 0) {
                    Log.d(TAG, "Recycler view is not empty with item count " + recyclerView.getAdapter().getItemCount());
                    noMemeAnimation.setVisibility(View.GONE);       //disappear
                    noMemeTV.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);       //make visible
                }

//                if (getItemCount() == 0) {
//                    Log.d(TAG, "Recycler view is empty with item count " + getItemCount());
//                    /*show no data lottie animation*/
//                    noMemeAnimation.setVisibility(View.VISIBLE);       //make visible
//                    noMemeTV.setVisibility(View.VISIBLE);
//                    recyclerView.setVisibility(View.GONE);      //disappear`
//                } else if (getItemCount() > 0){
//                    Log.d(TAG, "Recycler view is not empty with item count " + getItemCount());
//                    noMemeAnimation.setVisibility(View.GONE);       //disappear
//                    noMemeTV.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);       //make visible
//                }
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
                    Glide.with(requireContext()).asBitmap()
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
        };


        //attaching the adapter to my recycler view
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void holderItemLongClick(MemeHolder holder, int position, MemeUploads model) {
        /*getting current user id from shared preferences*/
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserDetails", MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("userID", null);
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
                    Flashbar progressFlashBar = new Flashbar.Builder(requireActivity())
                            .gravity(Flashbar.Gravity.TOP)
                            .title("Cannot delete.")
                            .message("Post belongs to someone else")
                            .duration(2000L)
                            .showIcon()
                            .icon(R.drawable.ic_warning_24dp)
                            .iconColorFilterRes(R.color.yellow)
                            .iconAnimation(FlashAnim.with(requireActivity()).animateIcon()
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
                        confirmationBottomSheetFragment.show(getFragmentManager(), confirmationBottomSheetFragment.getTag());
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

    private void holderItemClick(MemeHolder holder, int position, MemeUploads model) {
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

                Intent intent = new Intent(getActivity(), ViewMemeActivity.class);
                intent.putExtra("userId", uploadedBy);
                intent.putExtra("memeId", documentPath);
                intent.putExtra("memeTitle", memeTitle);
                intent.putExtra("memeDate", getDateInString);
                intent.putExtra("memeUrl", memeUrl);
                intent.putExtra("memeType", memeType);
                startActivity(intent);
                Bungee.slideUp(requireActivity());
            }
        });
    }


    private void fabListener() {
        homeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*,video/*");
                startActivityForResult(intent, PickMeme);
            }
        });
    }


    private void init(@NotNull View view ) {
        homeFAB = view.findViewById(R.id.home_fab);
        recyclerView = view.findViewById(R.id.home_recyclerview);
        togglePrivateLayout = view.findViewById(R.id.home_set_private_linearLayout);
        togglePrivateTV = view.findViewById(R.id.home_set_private_textView);
        noMemeAnimation = view.findViewById(R.id.no_meme_lottie_animation);
        noMemeTV = view.findViewById(R.id.no_meme_text_view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PickMeme && resultCode == RESULT_OK){
            Uri theUri = data.getData();
            //String mimeType = data.getType();
            String mimeType = getMimeType(getContext(), theUri);
            String path = data.getData().getPath();
            Log.e(TAG, "URI : " + theUri.toString());
            Log.e(TAG, "MIME_TYPE : " + mimeType);
            Log.e(TAG, "PATH : " + path);
            //sending the details to the next activity
            Intent intent = new Intent(getContext(), AddMemeFromDeviceActivity.class);
            intent.setDataAndType(theUri, mimeType);
            startActivity(intent);
        }
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

    /*get mime type of content or file that was selected*/
    public String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
