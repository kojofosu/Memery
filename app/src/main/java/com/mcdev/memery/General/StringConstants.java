package com.mcdev.memery.General;

public class StringConstants {
    /*FIRESTORE*/
    public final static String LOGIN_BACKGROUND_COLLECTION = "loginBackground";
    public final static String USERS_COLLECTION = "users";
    public final static String MEMERIES_COLLECTION = "memeries";

    /*FIREBASE STORAGE*/
    public final static String STORAGE_MEME_UPLOADS = "Memeries";

    /*PRIVATE OR PUBLIC POSTS*/
    public final static String PUBLIC_POST= "Public";
    public final static String PRIVATE_POST= "Private";

    /*LOTTIE DIALOG FRAGMENT*/
    public enum DialogType{
        SIGN_IN,
        SIGN_OUT,
        UPLOAD_FILES;
    }

    /*BOTTOM SHEET CONFIRMATION DIALOG*/
    public enum ConfirmationDialog {
        CONFIRM_DELETE,
        CONFIRM_LOGOUT;
    }

}
