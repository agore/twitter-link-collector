package org.bitxbit;

import org.bitxbit.model.IngestionResponse;
import org.bitxbit.model.ReadTweet;
import org.bitxbit.model.Tweet;
import org.bitxbit.model.TweetDao;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
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

    @POST
    @Path("/ingest")
    public Response ingest(@Context Request request) {
        IngestionResponse resp = new Ingestor().ingest();
        Response.ResponseBuilder builder = Response.created(URI.create("/tweets/all?lowest_id=" + resp.getSinceId()));
        builder.entity(resp);
        return builder.build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/read")
    public Response read(List<ReadTweet> reads) {
        long[] ids = new long[reads.size()];
        for (int i = 0; i < reads.size(); i++) {
            ids[i] = reads.get(i).getId();
        }
//        for (ReadTweet r : reads) {
//            System.out.println(r.getId() + " :: " + r.isRead());
//        }
        new TweetDao().updateReadState(ids);
        return Response.ok().build();
    }
}
