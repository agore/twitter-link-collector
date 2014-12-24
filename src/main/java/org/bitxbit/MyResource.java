package org.bitxbit;

import org.bitxbit.model.Tweet;
import org.bitxbit.model.TweetDao;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.util.List;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

    @GET
    @Path("/tweets")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Tweet> getTweets(@QueryParam("count") int count) {
        TweetDao tweetDao = new TweetDao();
        return tweetDao.getTweets(count);
    }
//    static {
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

//    @GET
//    @Path(("/plain"))
//    @Produces(MediaType.TEXT_PLAIN)
//    public long getIt() {
//        return getTableCount();
//    }
//
//    @GET
//    @Path("/json")
//    @Produces(MediaType.APPLICATION_JSON)
//    public TableCount getJson() {
//        TableCount tc = new TableCount(getTableCount());
//        return tc;
//
//    }

//    private long getTableCount() {
//        Connection con = getConnection();
//        if (con == null) {
//            return -1;
//        }
//
//        Statement stmt = null;
//        ResultSet rs = null;
//        try {
//            stmt = con.createStatement();
//            rs = stmt.executeQuery("select count(*) from lh.tweets");
//            rs.first();
//            return rs.getLong(1);
//        } catch (Exception e) {
//            return -1;
//        } finally {
//            try { if (rs != null) rs.close();}catch(Exception e) {}
//            try { if (stmt != null) stmt.close();}catch(Exception e) {}
//            try { if (con != null) con.close();}catch(Exception e) {}
//        }
//    }
//
//    private Connection getConnection() {
//        try {
//            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306", "aditya", "");
//            return con;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static class TableCount {
//        private long count;
//
//        public TableCount(long count) {
//            this.count = count;
//        }
//
//        public long getCount() {
//            return count;
//        }
//    }
}
