package org.bitxbit.links.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bitxbit.links.model.Tweet;

public class TweetDao {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/lh", "aditya", "abc123");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
    public List<Tweet> getTweets() {
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        List<Tweet> list = new ArrayList<Tweet>();
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from main where url1 != '' and url1 != null");
            while (rs.next()) {
                Tweet t = new Tweet();
                t.setId(rs.getLong("id"));
                t.setDisplayName(rs.getString("display_name"));
                t.setScreenName(rs.getString("screen_name"));
                t.setText(rs.getString("profile_image"));
                t.setTimestamp(rs.getTimestamp("ts").toString());
                List<String> urls = new ArrayList<String>(2);
                String url = rs.getString("url1");
                if (url != null && !"".equals(url.trim())) urls.add(url);
                url = rs.getString("url2");
                if (url != null && !"".equals(url.trim())) urls.add(url);
                t.setUrls(urls);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch(Exception e) {}
            if (stmt != null) try { stmt.close(); } catch(Exception e) {}
            if (conn != null) try { conn.close(); } catch(Exception e) {}
        }
        return list;
    }
}
