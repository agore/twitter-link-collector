package org.bitxbit.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TweetDao {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
            return DriverManager.getConnection("jdbc:postgresql://localhost/postgres?user=aditya&password=tw33t5");
    }

    public List<Tweet> getTweets(int length, long beforeId) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            stmt = con.createStatement();
            String beforeSubQuery = beforeId != 0 ? String.format("and id < %1$s", beforeId) : "";
            String query = String.format("select * from tweet where (url1 is not null and url1 != '') %1$s order by id desc limit %2$s", beforeSubQuery, length);
            rs = stmt.executeQuery(query);
            List<Tweet> tweets = new ArrayList<Tweet>(length);
            while (rs.next()) {
                Tweet.TweetBuilder tb = new Tweet.TweetBuilder();
                tb.id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .screenName(rs.getString("screen_name"))
                    .tweet(rs.getString("tweet"))
                    .avatarUrl(rs.getString("avatar_url"))
                    .ts(rs.getTimestamp("ts"))
                    .originalName(rs.getString("orig_name"))
                    .originalScreenName(rs.getString("orig_screen_name"))
                    .originalAvatarUrl(rs.getString("orig_avatar_url"))
                    .tweetImageUrl(rs.getString("media_url"));
                String url1 = rs.getString("url1");
                if (url1 != null && !url1.isEmpty()) tb.url(url1);
                String url2 = rs.getString("url2");
                if (url2 != null && !url2.isEmpty()) tb.url(url2);

                tweets.add(tb.build());
            }

            return tweets;
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        } finally {
            try { if (rs != null) rs.close();}catch(Exception e) {}
            try { if (stmt != null) stmt.close();}catch(Exception e) {}
            try { if (con != null) con.close();}catch(Exception e) {}
        }
    }

}
