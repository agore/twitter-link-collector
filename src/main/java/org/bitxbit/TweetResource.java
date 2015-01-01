package org.bitxbit;

import org.bitxbit.model.Tweet;
import org.bitxbit.model.TweetDao;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.CacheControl;
import java.util.Date;
import java.util.List;

/**
 * Request tweets as follows:
 * /tweets/all?count=3 to get the 3 newest tweets
 * OR
 * /tweets/all?count=3&lowest_id=550482545356181504 to get 2 tweets that have ids lower than lowest_id (i.e. older than lowest_id)
 */
@Path("tweets")
public class TweetResource {
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTweets(@Context Request request, @QueryParam("count") int count, @QueryParam("lowest_id") long lowestId) {
        TweetDao tweetDao = new TweetDao();
        List<Tweet> tweets = tweetDao.getTweets(count, lowestId);
        if (tweets == null || tweets.isEmpty()) return Response.noContent().build();

        EntityTag eTag = new EntityTag(String.valueOf(tweets.get(0).getId()));

        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
        if (builder == null) {
            builder = Response.ok(tweets);
        }

        CacheControl cc = new CacheControl();
        cc.setMaxAge(60 * 20);
        return builder.cacheControl(cc).tag(eTag).build();
    }
}
