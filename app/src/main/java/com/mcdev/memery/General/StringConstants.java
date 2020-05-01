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

    /*USER FIELDS*/
    public final static String ACCOUNT_CREATION = "accountCreation";
    public final static String LAST_LOGIN = "lastLogIn";
    public final static String USER_DOC_ID = "userDocID";
    public final static String USER_EMAIL = "userEmail";
    public final static String USER_ID = "userId";
    public final static String USER_NAME = "userName";
    public final static String USER_PHONE_NUMBER = "userPhoneNumber";
    public final static String USER_PHOTO_URL = "userPhotoUrl";

    /*MEMERIES FIELDS*/
    public final static String DOWNLOAD_URL = "downloadUrl";
    public final static String MEME_ID = "memeId";
    public final static String MEME_TITLE = "memeTitle";
    public final static String MEME_TYPE = "memeType";
    public final static String POSTED_AT = "postedAt";
    public final static String PRIVATE = "private";
    public final static String THUMBNAIL = "thumbnail";
    public final static String UPDATED_AT = "updatedAt";
    public final static String UPLOADED_BY = "uploadedBy";


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
