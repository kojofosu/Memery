package com.mcdev.memery;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;

public class HomeFragment extends Fragment {

    private FloatingActionButton homeFAB;
    private static final int PickMeme = 212;
    private static final String TAG = HomeFragment.class.getSimpleName();
    private RecyclerView recyclerView;

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

        //listeners
        fabListener();

        //firebase stuff
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        firebaseFirestoreUI();



        return view;
    }

    private void firebaseFirestoreUI() {
        Query query = FirebaseFirestore.getInstance()
                .collection(StringConstants.MEMERIES_COLLECTION)
                .orderBy("postedAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MemeUploads> options = new FirestoreRecyclerOptions.Builder<MemeUploads>()
                .setQuery(query, MemeUploads.class)
                .setLifecycleOwner(getActivity())
                .build();

        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<MemeUploads, MemeHolder>(options) {

            @NonNull
            @Override
            public MemeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.home_items_layout,parent,false);

                return new MemeHolder(view);
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
                Long postedAt = model.getPostedAt();
                Log.d(TAG, "postedAt from db : " + postedAt);
                Long updatedAt = model.getUpdatedAt();
                Log.d(TAG, "updatedAt from db : " + updatedAt);
                Boolean isPrivate = model.isPrivate();
                Log.d(TAG, "isPrivate from db : " + isPrivate);

                if (type.equals("video")){
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.typeTextView.setText(".MP4");
                    long interval = 5000 * 1000;
                    RequestOptions options = new RequestOptions().frame(interval);
                    Glide.with(getContext()).asBitmap()
                            .load(downloadUrl)
                            .apply(options)
                            .into(holder.imageView);
//                    holder.videoView.setVisibility(View.VISIBLE);
//                    holder.videoView.setVideoURI(Uri.parse(downloadUrl));
                }else{
                    holder.imageView.setVisibility(View.VISIBLE);
                    Picasso.get().load(downloadUrl).into(holder.imageView);
                    holder.typeTextView.setText(".JPG");
                }

                //setting title
                holder.titleTextView.setText(title);
            }
        };
        //attaching the adapter to my recycler view
        recyclerView.setAdapter(adapter);

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
}
