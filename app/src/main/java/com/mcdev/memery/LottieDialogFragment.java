package com.mcdev.memery;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mcdev.memery.General.StringConstants;
import com.mcdev.memery.POJOS.MemeUploads;

import java.util.Objects;

public class LottieDialogFragment extends DialogFragment {

    private static final String TAG = LottieDialogFragment.class.getSimpleName();
    private TextView textView;

    public static LottieDialogFragment newInstance(){
        Bundle args = new Bundle();

        LottieDialogFragment fragment = new LottieDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lottie_dialog_fragment, container, false);

        textView = view.findViewById(R.id.progress_dialog_message);     //init text view

        /*Checking if Dialog type will be COMPLEX or SIMPLE*/


        /*Handling the download here because the dialog fragment crashes when i try to pass the upload progress to the the fragment from the activity*/
        Bundle bundle = getArguments();     //getting the arguments passed from the activity
        String URI = null;
        String selectedType = null;
        String currentUserId = null;
        String caption = null;
        String dialogType = null;
        if (bundle != null) {
            dialogType = bundle.getString("dialogType", "");
            if (dialogType.equals(String.valueOf(StringConstants.DialogType.SIGN_IN))){
                Log.d(TAG, "dialog type : " + dialogType + " equals " + StringConstants.DialogType.SIGN_IN);
                textView.setText(R.string.signing_in);      //set custom dialog text view
            } else if (dialogType.equals(String.valueOf(StringConstants.DialogType.SIGN_OUT))) {
                Log.d(TAG, "dialog type : " + dialogType + " equals " + StringConstants.DialogType.SIGN_IN);
                textView.setText(R.string.signing_out);      //set custom dialog text view
            } else if (dialogType.equals(String.valueOf(StringConstants.DialogType.UPLOAD_FILES))) {
                Log.d(TAG, "dialog type : " + dialogType + " equals " + StringConstants.DialogType.UPLOAD_FILES);

                currentUserId = bundle.getString("currentUserId", "");       //getting the current user's id
                caption = bundle.getString("caption", "");       //getting the caption
                selectedType = bundle.getString("selectedType", "");     //getting the selected item type
                URI = bundle.getString("URI", "");       //getting the uri
                //Start file upload to storage
                startFileUpload(currentUserId, caption, selectedType, URI);
            }

        }

        return view;
    }

    private void startFileUpload(String currentUserId, String caption, String selectedType, String URI) {
        //database
        FirebaseFirestore firebaseFirestore =  FirebaseFirestore.getInstance();     //init firestore
        DocumentReference documentReference = firebaseFirestore.collection(StringConstants.MEMERIES_COLLECTION).document();     //init document
        String memeID = documentReference.getId();          //generation meme id and storing it in String variable
        //storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference memeStorageRef = storageReference.child(StringConstants.STORAGE_MEME_UPLOADS).child(currentUserId).child(memeID);      //meme id was the last child so as to prevent the overriding of uploads

        memeStorageRef.putFile(Uri.parse(URI)).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double doubleProgress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();        //double type of the upload progress
                int progress = (int) doubleProgress;        //integer type of the upload progress
                String uploadProgress = "Uploading..." + progress + "%";
                Log.d(TAG, uploadProgress);
                textView.setText(uploadProgress);       //setting the text view with the upload progress

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //getting download url of meme
                memeStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Getting current timestamp
                        Long tsLong = System.currentTimeMillis()/1000;

                        //populating meme upload field
                        MemeUploads memeUploads = new MemeUploads();
                        memeUploads.setUploadedBy(currentUserId);
                        memeUploads.setMemeId(memeID);
                        memeUploads.setMemeTitle(caption);
                        memeUploads.setMemeType(selectedType);
                        memeUploads.setPostedAt(tsLong);
                        memeUploads.setDownloadUrl(uri.toString());
                        memeUploads.setPrivate(false);

                        //posting to db
                        firebaseFirestore.collection(StringConstants.MEMERIES_COLLECTION)
                                .document(memeID)
                                .set(memeUploads)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Objects.requireNonNull(getDialog()).dismiss();      //dismiss this dialgo fragment after upload success
                                        Log.d(TAG, "Upload Success");
                                        Toast.makeText(getContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                                        Objects.requireNonNull(getActivity()).finish();     //finish the activity from which the dialog fragment resides(AddMemeFromDeviceActivity)
                                    }
                                });
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Uploading meme failed with " + e.getLocalizedMessage());
                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));        //make the dialog fragment's background transparent
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);
    }
}
