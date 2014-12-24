package org.bitxbit.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Tweet {
    private long id;
    private String name;
    private String screenName;
    private String tweet;
    private String avatarUrl;
    private Date ts;
    private List<String> urls;
    private int clickCount;
    private boolean liked;
    private boolean disliked;

    public static class TweetBuilder {
        private Tweet instance;

        public TweetBuilder() {
            instance = new Tweet();
        }

        public TweetBuilder id(long id) {
            instance.id = id;
            return this;
        }

        public TweetBuilder name(String name) {
            instance.name = name;
            return this;
        }

        public TweetBuilder screenName(String sn) {
            instance.screenName = sn;
            return this;
        }

        public TweetBuilder tweet(String tweet) {
            instance.tweet = tweet;
            return this;
        }

        public TweetBuilder avatarUrl(String url) {
            instance.avatarUrl = url;
            return this;
        }

        public TweetBuilder ts(Date ts) {
            instance.ts = ts;
            return this;
        }

        public TweetBuilder urls(String... urls) {
            instance.urls = Arrays.asList(urls);
            return this;
        }

        public Tweet build() {
            return instance;
        }
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("sn")
    public String getScreenName() {
        return screenName;
    }

    @JsonProperty("t")
    public String getTweet() {
        return tweet;
    }

    @JsonProperty("au")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @JsonProperty("ts")
    public Date getTs() {
        return ts;
    }

    @JsonProperty("urls")
    public List<String> getUrls() {
        return urls;
    }

    @JsonProperty("cc")
    public int getClickCount() {
        return clickCount;
    }

    @JsonProperty("l")
    public boolean isLiked() {
        return liked;
    }

    @JsonProperty("not_l")
    public boolean isDisliked() {
        return disliked;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setDisliked(boolean disliked) {
        this.disliked = disliked;
    }
}
