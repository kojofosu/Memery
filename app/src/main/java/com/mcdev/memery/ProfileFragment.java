package com.mcdev.memery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jgabrielfreitas.core.BlurImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private BlurImageView profileBlurImageView;
    private ImageView profileImageView;
    TextView profileUsernameTV, profileUserEmailTV;
    //Picasso
    private  Picasso picasso;
    //Firebase
    private FirebaseFirestore firebaseFirestore;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        profileBlurImageView = view.findViewById(R.id.profile_blur_image_view);
        profileImageView = view.findViewById(R.id.profile_image_view);
        profileUsernameTV = view.findViewById(R.id.profile_user_name);
        profileUserEmailTV = view.findViewById(R.id.profile_user_email);
        firebaseFirestore = FirebaseFirestore.getInstance();
        picasso = Picasso.get();
        firebaseFirestore.collection("samplePic").document("123").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()){
                    Log.d(TAG, "login background empty");
                }else {
                    final String imgUrl = documentSnapshot.get("imageUrl").toString();
                    Log.d(TAG, "imgUrl " + imgUrl);
                    picasso.load(imgUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profileImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            picasso.load(imgUrl).into(profileBlurImageView);
                            profileBlurImageView.setBlur(20);
                        }

                        @Override
                        public void onError(Exception e) {
                            picasso.load(imgUrl).into(profileImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    picasso.load(imgUrl).into(profileBlurImageView);
                                    profileBlurImageView.setBlur(20);
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        }
                    });
                }
            }
        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            String username = firebaseUser.getDisplayName();
            String userEmail = firebaseUser.getEmail();
            String userPhone = firebaseUser.getPhoneNumber();
            String userPhotoUrl = firebaseUser.getPhotoUrl().toString();

            profileUsernameTV.setText(username);    //setting user name text
            //checking to see if user email is null, then it assigns phone number..but if both are null set visibility to GONE
            if (userEmail != null){    //if email is not null and phone is null
                profileUserEmailTV.setText(userEmail);
            }else {   //if both email and phone are null
                profileUserEmailTV.setVisibility(View.GONE);
            }
        }

        return view;
    }
}
