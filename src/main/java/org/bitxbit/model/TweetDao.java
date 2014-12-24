package org.bitxbit.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TweetDao {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306", "aditya", "");
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Tweet> getTweets(int length) {
        Connection con = getConnection();
        if (con == null) {
            return Collections.EMPTY_LIST;
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("select * from lh.tweet where (url1 is not null and url1 != \"\") order by id desc limit " + length);
            List<Tweet> tweets = new ArrayList<Tweet>(length);
            while (rs.next()) {
                Tweet.TweetBuilder tb = new Tweet.TweetBuilder();
                tb.id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .screenName(rs.getString("screen_name"))
                    .tweet(rs.getString("tweet"))
                    .avatarUrl(rs.getString("avatar_url"))
                    .ts(rs.getDate("ts"))
                    .urls(rs.getString("url1"), rs.getString("url2"));
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
