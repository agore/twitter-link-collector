package com.bitxbit.ingestion;

import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Main {
    private static final int MAX = 5;
    public static final int NUM_TWEETS = 200;

    public static void main(String[] args) {
        Main main = new Main();
        main.ingest();
    }

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void ingest() {
        Twitter twitter = TwitterFactory.getSingleton();
        auth(twitter);
        Config conf = readConfig();
        Paging paging = getPaging(conf.sinceId, 0);
        int total = 0;
        int discarded = 0;
        List<Status> statuses = readTimeline(twitter, paging);
        if (statuses.isEmpty()) {
            System.out.println("No tweets found at initialization. Exiting now");
            return;
        }
        total += statuses.size();
        discarded += dumpStatusesToDb(statuses);
        conf.sinceId = statuses.get(0).getId();
        long tempMaxId = statuses.get(statuses.size() - 1).getId() - 1;

        int loop = 0;
        while (true) {
            if (conf.maxId == 0 || tempMaxId < conf.maxId || loop == MAX) {
                System.out.println((conf.maxId == 0) ? "conf.maxId == 0"
                        : (tempMaxId < conf.maxId) ?
                        "tempMaxId (" + tempMaxId + ") < conf.maxId (" + conf.maxId + ")"
                        : "loop maximum reached");
                conf.maxId = conf.sinceId;
                break;
            }
            conf.maxId = tempMaxId;
            statuses = readTimeline(twitter, getPaging(0, conf.maxId));
            if (statuses.isEmpty()) {
                System.out.println("No statuses on loop " + loop);
                break;
            }
            total += statuses.size();
            discarded += dumpStatusesToDb(statuses);
            tempMaxId = statuses.get(statuses.size() - 1).getId() - 1;
            loop++;
        }

        writeConfig(conf);
//        main.readTimeline(twitter, null);
        System.out.println("Processed: " + total);
        System.out.println("Wrote: " + (total - discarded));
        System.out.println("Discarded: " + discarded);

    }

    private Paging getPaging(long sinceId, long maxId) {
        Paging paging = null;
        if (maxId == 0 && sinceId == 0) {
            paging = new Paging(1, NUM_TWEETS);
        } else if (maxId == 0 && sinceId != 0) {
            paging = new Paging(1, NUM_TWEETS, sinceId);
        } else if (maxId != 0 && sinceId == 0) {
            paging = new Paging(1, NUM_TWEETS);
            paging.setMaxId(maxId);
        } else {
            paging = new Paging(1, NUM_TWEETS, sinceId, maxId);
        }
        return paging;
    }

    private Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306", "aditya", "tw33t5");
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void auth(Twitter twitter) {
        twitter.setOAuthConsumer("qWVMWBNdGu4PibPH13CtzA", "X23D2dWqNY3t7XoMYF3sLcL2yT9LLBQgazsyT964");
        AccessToken token = new AccessToken("17902263-JNXCNI6s0zqdqxJL6Tbz2Rlh32tPYbed2KJwZALOr", "hrXRb1cObhst4LIZJyDy6JyVsvyvUpzTqi1hnZVHHW2sq");
        twitter.setOAuthAccessToken(token);

    }

    private Config readConfig() {
        Properties props = new Properties();
        String home = getConfigFilename();
        try {
            props.load(new FileInputStream(home));
            Config config = new Config();
            config.maxId = safelyParseLong(props.getProperty("max_id"));
            config.sinceId = safelyParseLong(props.getProperty("since_id"));
            return config;
        } catch (IOException e) {
//            e.printStackTrace();
        }

        return new Config();
    }

    private String getConfigFilename() {
        String home = System.getProperty("user.home");
        if (!home.endsWith(File.separator)) home += File.separator;
        home += ".linkharvester" + File.separator + "ids.properties";
        return home;
    }

    private void writeConfig(Config config) {
        Properties props = new Properties();
        props.setProperty("max_id", String.valueOf(config.maxId));
        props.setProperty("since_id", String.valueOf(config.sinceId));
        try {
            props.store(new FileOutputStream(getConfigFilename()), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Config : max_id " + config.maxId + " :: since_id " + config.sinceId);
    }

    public int dumpStatusesToDb(List<Status> statuses) {
        Connection conn = null;
        PreparedStatement stmt = null;
        int discarded = 0;

        try {
            conn = getConnection();
            if (conn == null) return 0;
            stmt = conn.prepareStatement("INSERT INTO lh.tweet (id, name, screen_name, tweet, avatar_url, " +
                    "orig_name, orig_screen_name, orig_avatar_url, ts, url1, url2, media_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ts=?");
            for (Status s : statuses) {
                if (s.getURLEntities() == null || s.getURLEntities().length == 0) {
                    discarded++;
                    continue;
                }
                URLEntity[] urls = s.getURLEntities();
                stmt.setLong(1, s.getId());
                stmt.setString(2, s.getUser().getName());
                stmt.setString(3, s.getUser().getScreenName());
                stmt.setString(4, s.getText());
                stmt.setString(5, s.getUser().getProfileImageURL());
                Status rt = s.getRetweetedStatus();
                if (rt != null && rt.getUser() != null) {
                    stmt.setString(6, rt.getUser().getName());
                    stmt.setString(7, rt.getUser().getScreenName());
                    stmt.setString(8, rt.getUser().getProfileImageURL());
                } else {
                    stmt.setString(6, null);
                    stmt.setString(7, null);
                    stmt.setString(8, null);
                }
                String ts = format(s.getCreatedAt());
                stmt.setString(9, ts);
                stmt.setString(10, s.getURLEntities()[0].getExpandedURL());
                stmt.setString(11, (urls.length > 1 ? s.getURLEntities()[1].getExpandedURL() : null));


                if (s.getMediaEntities() != null && s.getMediaEntities().length > 0) {
                    stmt.setString(12, s.getMediaEntities()[0].getMediaURL());
                } else {
                    stmt.setString(12, null);
                }
                stmt.setString(13, ts);
                stmt.addBatch();
//                System.out.println(String.format("%1$s :: %2$s :: %3$s :: %4$s :: %5$s :: %6$s :: %7$s\n",
//                        s.getId(), s.getUser().getName(), s.getUser().getScreenName(), s.getText(), s.getUser().getProfileImageURL(),
//                        format(s.getCreatedAt()), urls[0].getDisplayURL(),
//                        (urls.length > 1 ? s.getURLEntities()[1].getDisplayURL() : "")));

            }

            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) try { stmt.close(); } catch(Exception e) {}
            if (conn != null) try { conn.close(); } catch(Exception e) {}
        }

        return discarded;
    }

    private String format(Date createdAt) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createdAt);

    }

    public List<Status> readTimeline(Twitter twitter, Paging paging) {
        if (paging == null) {
            paging = new Paging(1, 20);
        }
        try {
            ResponseList<Status> homeTimeline = twitter.getHomeTimeline(paging);
            return homeTimeline;

        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private long safelyParseLong(String val) {
        if (val == null || "".equals(val.trim())) return 0;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private static class Config {
        private long maxId;
        private long sinceId;
    }

}
