package com.mcdev.memery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mcdev.memery.General.GetIntents;
import com.mcdev.memery.General.StringConstants;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;



/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmationBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = ConfirmationBottomSheetFragment.class.getSimpleName();
    private Button confirmActionBtn, cancelActionBtn;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    private LottieDialogFragment lottieDialogFragment;

    public ConfirmationBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirmation_bottom_sheet, container, false);

        //init
        init(view);

        //init custom dialog
        lottieDialogFragment = new LottieDialogFragment();
        //sharedPrefs
        sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

        //init Firebase stuff
        initFirebaseStuff();

        /*getting intent data*/
        Bundle args = getArguments();
        if (args != null) {
            String confirmationType = args.getString("confirmationDialogType");
            Log.d(TAG, "confirmationType : " + confirmationType);
            //checking if user wants to logout
            if (confirmationType != null && confirmationType.equals(String.valueOf(StringConstants.ConfirmationDialog.CONFIRM_LOGOUT))) {
                //log out listeners
                confirmActionBtn.setText(R.string.log_out);         //setting confirmation btn to Log out
                confirmLogoutListener();
                cancelLogoutListener();
            } else if (confirmationType != null && confirmationType.equals(String.valueOf(StringConstants.ConfirmationDialog.CONFIRM_DELETE))){
                /*getting the extra data passed*/
                String currentUserId = args.getString("currentUserId");
                Log.d(TAG, "currentUserId : " + currentUserId);
                String documentPath = args.getString("documentPath");
                Log.d(TAG, "documentPath : " + documentPath);
                //delete listeners
                confirmActionBtn.setText(R.string.delete);         //setting confirmation btn to delete
                confirmDeleteListener(currentUserId, documentPath);
                cancelDeleteListener();
            }
        }



        return view;
    }

    private void cancelDeleteListener() {
        cancelActionBtn.setOnClickListener(view -> dismiss());
    }

    private void confirmDeleteListener(String currentUserId, String documentPath) {
        confirmActionBtn.setOnClickListener(view -> {
            Objects.requireNonNull(getDialog()).dismiss();      ///dismiss dialog after it has been clicked
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();      //init firebase firestore
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();        //init firebase storage
            StorageReference storageReference = firebaseStorage.getReference();      //init base storage reference
            assert currentUserId != null;
            /*storage ref to be deleted*/
            StorageReference refToDeleteStorage = storageReference
                    .child(StringConstants.STORAGE_MEME_UPLOADS)
                    .child(currentUserId)
                    .child(documentPath);

            firebaseFirestore.collection(StringConstants.MEMERIES_COLLECTION).document(documentPath).delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Deleted Successfully from database");
                        /*delete media from firebase storage as well*/
                        refToDeleteStorage.delete().addOnSuccessListener(aVoid1 -> {
                            Log.d(TAG, "Deleted Successfully from storage");

                        });

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Deleting failed : " + e.getLocalizedMessage());
                        Toast.makeText(getContext(), "Failed to Delete", Toast.LENGTH_SHORT).show();
                    });
        });
    }


    private void cancelLogoutListener() {
        cancelActionBtn.setOnClickListener(view -> dismiss());
    }

    private void confirmLogoutListener() {
        confirmActionBtn.setOnClickListener(view -> {
            /*configure and show custom dialog progress*/
            lottieDialogFragment.setCancelable(false);
            Bundle bundle = new Bundle();
            bundle.putString("dialogType", String.valueOf(StringConstants.DialogType.SIGN_OUT));
            lottieDialogFragment.setArguments(bundle);
            assert getFragmentManager() != null;
            lottieDialogFragment.show(getFragmentManager(),"");

            // continue with logout
            try {
                //checking to see if user is logged in with twitter or facebook
                if (isUserLoggedInWithFacebook()){
                    Log.d(TAG, "isUserLoggedInWithFacebook : " + isUserLoggedInWithFacebook());
                    //then log user out of facebook
                    FacebookSdk.fullyInitialize();      //initializing facebook SDK
                    // remove permissions and revoke access for user to be able to login again with another account if they choose
                    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, response -> {
                        LoginManager.getInstance().logOut();        //Log user out of facebook
                        firebaseAuth.signOut();     //log user out of firebase
                        SendUserToLoginActivity();      //send the user to login page
                    }).executeAsync();      //execute permission deletion
                }else if (isUserLoggedInWithTwitter()){
                    Log.d(TAG, "isUserLoggedInWithTwitter : " + isUserLoggedInWithTwitter());
                    //then log user out of twitter
                    TwitterCore.getInstance().getSessionManager().clearActiveSession();     //clearing current user session
                    firebaseAuth.signOut();     //log user out of firebase
                    SendUserToLoginActivity();      //send the user to login page
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        });

    }

    @SuppressLint("CommitPrefEdits")
    private void SendUserToLoginActivity() {
        sharedPreferences.edit().clear();    //clearing data in shared preference
        sharedPreferences.edit().apply();       //applying changes
        GetIntents getIntents = new GetIntents();       //init  getIntents
        lottieDialogFragment.dismiss();     //dismiss dialog
        getIntents.goToLogin(getActivity());        //start intent
        Objects.requireNonNull(getActivity()).finish();         //finish
    }

    private boolean isUserLoggedInWithFacebook(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();      //getting access token
        return accessToken != null && !accessToken.isExpired();
    }

    private void initFirebaseStuff() {
        firebaseAuth = FirebaseAuth.getInstance();      //initializing firebase auth
    }

    private boolean isUserLoggedInWithTwitter(){
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();      //checking if user session is active
        return session != null;
    }

    private void init(View view) {
        confirmActionBtn = view.findViewById(R.id.confirm_logout);
        cancelActionBtn = view.findViewById(R.id.cancel_logout);
    }
}
