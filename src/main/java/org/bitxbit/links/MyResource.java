package org.bitxbit.links;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bitxbit.links.dao.TweetDao;
import org.bitxbit.links.model.Tweet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("lh")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all")
    public String getAll() {
        TweetDao dao = new TweetDao();
        List<Tweet> tweets = dao.getTweets();
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(tweets);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
