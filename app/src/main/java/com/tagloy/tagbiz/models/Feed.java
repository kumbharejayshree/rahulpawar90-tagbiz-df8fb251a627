package com.tagloy.tagbiz.models;

public class Feed {
    public int feed_id;
    public String user_name;
    public String feed_message;
    public String upload_time;
    public String imgUri;

    public String getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(String upload_time) {
        this.upload_time = upload_time;
    }

    public int getFeed_id() {
        return feed_id;
    }

    public void setFeed_id(int feed_id) {
        this.feed_id = feed_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getFeed_message() {
        return feed_message;
    }

    public void setFeed_message(String feed_message) {
        this.feed_message = feed_message;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }
}
