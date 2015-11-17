package org.bitxbit;

import org.bitxbit.db.ConnectionUtils;
import org.bitxbit.model.IngestionResponse;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.sql.*;
import java.util.Collections;
import java.util.List;

public class Ingestor {
    private static final int MAX = 5;
    public static final int NUM_TWEETS = 200;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public IngestionResponse ingest() {
        Twitter twitter = TwitterFactory.getSingleton();
        auth(twitter);

        //this is slightly ineffecient because we might have seen a more recent tweet
        //that didn't make it to the DB
        long sinceId = getMostRecentTweetId();

        Paging paging = new Paging(1, NUM_TWEETS);
        if (sinceId != 0) {
            //Our table's not empty
            paging.sinceId(sinceId);
        }

        List<Status> statuses = readTimeline(twitter, paging);
        if (statuses == null || statuses.size() == 0) {
            System.out.println("No tweets found to start with. Exiting");
            return new  IngestionResponse(0, 0, 0, 0, sinceId);
        }

        int discarded = dumpStatusesToDb(statuses);
        int total = statuses.size();
        int looper = 1;
        long maxId = getMaxId(statuses);
        while (looper < MAX) {
            System.out.println("Starting loop " + looper);
            paging = new Paging(1, NUM_TWEETS).maxId(maxId);
            if (sinceId != 0) paging.sinceId(sinceId);
            statuses = readTimeline(twitter, paging);
            if (statuses == null || statuses.size() == 0) {
                break;
            }
            maxId = getMaxId(statuses);
            total += statuses.size();
            looper++;
            discarded += dumpStatusesToDb(statuses);
        }

        return new IngestionResponse(looper, total, (total - discarded), discarded, sinceId);
    }

    private long getMaxId(List<Status> statuses) {
        if (statuses == null || statuses.size() == 0) return -1;
        return statuses.get(statuses.size() - 1).getId() - 1;
    }

    private long getMostRecentTweetId() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = ConnectionUtils.getConnection();
            if (con == null) return 0;
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT id from tweet order by ts desc limit 1");
            if (rs.next()) {
                long id = rs.getLong("id");
                return id;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            System.out.println(e.getNextException());
        } finally {
            if (rs != null) try {rs.close(); }catch(Exception e) {}
            if (stmt != null) try {stmt.close(); }catch(Exception e) {}
            if (con != null) try {con.close(); }catch(Exception e) {}
        }

        return 0;
    }

    private Paging getPaging(long sinceId, long maxId) {
        Paging paging = null;
        if (maxId == 0 && sinceId == 0) {
            paging = new Paging(1, NUM_TWEETS);
        } else if (maxId == 0 && sinceId != 0) {
            paging = new Paging(1, NUM_TWEETS).sinceId(sinceId);
        } else if (maxId != 0 && sinceId == 0) {
            paging = new Paging(1, NUM_TWEETS).maxId(maxId);
        } else {
            paging = new Paging(1, NUM_TWEETS).sinceId(sinceId).maxId(maxId);
        }
        return paging;
    }

    public void auth(Twitter twitter) {
        if (twitter.getAuthorization() != null && twitter.getAuthorization().isEnabled()) return;
        twitter.setOAuthConsumer("qWVMWBNdGu4PibPH13CtzA", "X23D2dWqNY3t7XoMYF3sLcL2yT9LLBQgazsyT964");
        AccessToken token = new AccessToken("17902263-JNXCNI6s0zqdqxJL6Tbz2Rlh32tPYbed2KJwZALOr", "hrXRb1cObhst4LIZJyDy6JyVsvyvUpzTqi1hnZVHHW2sq");
        twitter.setOAuthAccessToken(token);
    }

    public int dumpStatusesToDb(List<Status> statuses) {
        Connection conn = null;
        PreparedStatement stmt = null;
        int discarded = 0;

        try {
            conn = ConnectionUtils.getConnection();
            if (conn == null) return 0;
            stmt = conn.prepareStatement("INSERT INTO tweet (id, name, screen_name, tweet, avatar_url, " +
                    "orig_name, orig_screen_name, orig_avatar_url, ts, url1, url2, media_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
                Timestamp ts = new Timestamp(s.getCreatedAt().getTime());
                stmt.setTimestamp(9, ts);
                stmt.setString(10, s.getURLEntities()[0].getExpandedURL());
                stmt.setString(11, (urls.length > 1 ? s.getURLEntities()[1].getExpandedURL() : null));


                if (s.getMediaEntities() != null && s.getMediaEntities().length > 0) {
                    stmt.setString(12, s.getMediaEntities()[0].getMediaURL());
                } else {
                    stmt.setString(12, null);
                }
                stmt.addBatch();
//                System.out.println(String.format("%1$s :: %2$s :: %3$s :: %4$s :: %5$s :: %6$s :: %7$s\n",
//                        s.getId(), s.getUser().getName(), s.getUser().getScreenName(), s.getText(), s.getUser().getProfileImageURL(),
//                        format(s.getCreatedAt()), urls[0].getDisplayURL(),
//                        (urls.length > 1 ? s.getURLEntities()[1].getDisplayURL() : "")));

            }

            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getNextException());
        } finally {
            if (stmt != null) try { stmt.close(); } catch(Exception e) {}
            if (conn != null) try { conn.close(); } catch(Exception e) {}
        }

        return discarded;
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

}
