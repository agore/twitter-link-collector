package org.bitxbit;

import org.bitxbit.model.IngestionResponse;
import org.bitxbit.model.ReadTweet;
import org.bitxbit.model.Tweet;
import org.bitxbit.model.TweetDao;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Request tweets as follows:
 * /tweets/all?count=3 to get the 3 newest tweets
 * OR
 * /tweets/all?count=3&older_than_id=550482545356181504 to get 2 tweets that have ids lower than older_than_id
 */
@Path("tweets")
public class TweetResource {
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTweets(@Context Request request, @QueryParam("count") int count, @QueryParam("older_than_id") long olderThanId) {
        TweetDao tweetDao = new TweetDao();
        List<Tweet> tweets = tweetDao.getTweets(count, olderThanId);
        if (tweets == null || tweets.isEmpty()) return Response.ok(Collections.EMPTY_LIST).build();

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
    public Response markAsRead(List<ReadTweet> reads) {
        if (reads == null || reads.size() == 0) return Response.ok("{\"updated\": 0}").build();

        long[] ids = new long[reads.size()];
        for (int i = 0; i < reads.size(); i++) {
            ids[i] = reads.get(i).getId();
        }
        int updated = new TweetDao().markAsRead(ids);
        return Response.ok("{\"updated\" : " + updated + "}").build();
    }

    @GET
    @Path("/read_ids")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadIds(@Context Request request, @QueryParam("highest_id") long highestId, @QueryParam("lowest_id") long lowestId) {
        List<Long> ids = new TweetDao().getReadIds(highestId, lowestId);
        if (ids == null || ids.isEmpty()) return Response.ok(Collections.EMPTY_LIST).build();

        EntityTag eTag = new EntityTag(String.valueOf(ids.get(0)));

        Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
        if (builder == null) {
            builder = Response.ok(ids);
        }

        CacheControl cc = new CacheControl();
        cc.setMaxAge(30);
        return builder.cacheControl(cc).tag(eTag).build();
    }
}
