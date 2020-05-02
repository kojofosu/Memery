package com.mcdev.memery;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mcdev.memery.General.StringConstants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class MemeDetailsFragment extends BottomSheetDialogFragment {

    private static final String TAG = MemeDetailsFragment.class.getSimpleName();
    private TextView memeTitleTV, memeDateTV, memeTypeTV, userNameTV, userEmailTV;
    private ImageView userImageView;
    private FirebaseFirestore firebaseFirestore;
    public MemeDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meme_details, container, false);

        //init
        init(view);

        //init firebase firestore
        firebaseFirestore = FirebaseFirestore.getInstance();

        //getting extras
        Bundle args = getArguments();
        if (args != null) {
            String userId = args.getString("userId");
            String memeId = args.getString("memeId");
            String memeTitle = args.getString("memeTitle");
            String memeDate = String.valueOf(args.getLong("memeDate"));
            String memeUrl = args.getString("memeUrl");
            String memeType = args.getString("memeType");

            /*setting meme details to fields and views*/
            setMemeInfo(memeId, memeTitle, memeDate, memeUrl, memeType);
            //fetch user details
            getUserInfo(userId);
            //fetch meme details
            //getMemeInfo(memeId);
        }

        return view;
    }

    private void setMemeInfo(String memeId, String memeTitle, String memeDate, String memeUrl, String memeType) {
        //setting variables to text views
        memeTitleTV.setText(memeTitle);
        memeDateTV.setText(memeDate);
        memeTypeTV.setText(memeType);
    }

    private void getMemeInfo(String memeId) {
        firebaseFirestore.collection(StringConstants.MEMERIES_COLLECTION)
                .document(memeId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String memeTitle = Objects.requireNonNull(documentSnapshot.get(StringConstants.MEME_TITLE)).toString();
                        String memeType = Objects.requireNonNull(documentSnapshot.get(StringConstants.MEME_TYPE)).toString();
                        String memeDate = Objects.requireNonNull(documentSnapshot.get(StringConstants.POSTED_AT)).toString();

                        memeTitleTV.setText(memeTitle);
                        memeTypeTV.setText(memeType);
                        memeDateTV.setText(memeDate);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "getMemeInfo error " + e.getLocalizedMessage());
                Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserInfo(String userId) {

        firebaseFirestore.collection(StringConstants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        /*getting required fields*/
                        String userName = Objects.requireNonNull(documentSnapshot.get(StringConstants.USER_NAME)).toString();
//                        String userEmail = Objects.requireNonNull(documentSnapshot.get(StringConstants.USER_EMAIL)).toString();
                        String userImageUrl = Objects.requireNonNull(documentSnapshot.get(StringConstants.USER_PHOTO_URL)).toString();

                        /*setting fields to text fields*/
                        userNameTV.setText(userName);           //setting username
//                        userEmailTV.setText(userEmail);         //setting user email

                        /*loading user image*/
                        Picasso.get().load(userImageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(userImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(userImageUrl).into(userImageView);
                            }
                        });
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "getUserInfo error " + e.getLocalizedMessage());
                Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init(View view) {
        userNameTV = view.findViewById(R.id.meme_detail_username);
        userImageView = view.findViewById(R.id.meme_detail_image_view);
        userEmailTV = view.findViewById(R.id.meme_detail_useremail);
        memeTitleTV = view.findViewById(R.id.meme_detail_meme_title);
        memeDateTV = view.findViewById(R.id.meme_detail_meme_date);
        memeTypeTV = view.findViewById(R.id.meme_detail_meme_type);
    }

}
