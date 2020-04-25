package com.mcdev.memery.POJOS;

public class MemeUploads {
    private String memeId;
    private String memeTitle;
    private String memeType;
    private String uploadedBy;
    private String downloadUrl;
    private String thumbnail;
    private boolean isPrivate;
    private long postedAt;
    private long updatedAt;

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getMemeId() {
        return memeId;
    }

    public void setMemeId(String memeId) {
        this.memeId = memeId;
    }

    public long getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(long postedAt) {
        this.postedAt = postedAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMemeTitle() {
        return memeTitle;
    }

    public void setMemeTitle(String memeTitle) {
        this.memeTitle = memeTitle;
    }

    public String getMemeType() {
        return memeType;
    }

    public void setMemeType(String memeType) {
        this.memeType = memeType;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
