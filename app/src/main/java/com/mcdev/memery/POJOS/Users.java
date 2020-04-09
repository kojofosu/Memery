package com.mcdev.memery.POJOS;

public class Users {
    private String userName;
    private String userEmail;
    private String userPhotoUrl;
    private String userId;
    private String userPhoneNumber;
    private String userDocID;
    private long accountCreation;
    private long lastLogIn;

    public String getUserDocID() {
        return userDocID;
    }

    public void setUserDocID(String userDocID) {
        this.userDocID = userDocID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public long getAccountCreation() {
        return accountCreation;
    }

    public void setAccountCreation(long accountCreation) {
        this.accountCreation = accountCreation;
    }

    public long getLastLogIn() {
        return lastLogIn;
    }

    public void setLastLogIn(long lastLogIn) {
        this.lastLogIn = lastLogIn;
    }
}
