package org.bitxbit.links.model;

import java.util.List;

public class Tweet {
    private long id;
    private String screenName;
    private String displayName;
    private String text;
    private String profileImage;
    private List<String> urls;
    private String timestamp;

    public Tweet(long id, String screenName, String displayName, String text, String profileImage,
            List<String> urls, String timestamp) {
        super();
        this.id = id;
        this.screenName = screenName;
        this.displayName = displayName;
        this.text = text;
        this.profileImage = profileImage;
        this.urls = urls;
        this.timestamp = timestamp;
    }

    public Tweet() {
        // TODO Auto-generated constructor stub
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
