package org.bitxbit.model;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by agore on 11/17/15.
 */
public class IngestionResponse {
    private int loop;
    private int processed;
    private int written;
    private int discarded;
    private long sinceId;

    public IngestionResponse(int loop, int processed, int written, int discarded, long sinceId) {
        this.loop = loop;
        this.processed = processed;
        this.written = written;
        this.discarded = discarded;
        this.sinceId = sinceId;
    }

    @JsonProperty("loop")
    public int getLoop() {
        return loop;
    }

    @JsonProperty("processed")
    public int getProcessed() {
        return processed;
    }

    @JsonProperty("written")
    public int getWritten() {
        return written;
    }

    @JsonProperty("discarded")
    public int getDiscarded() {
        return discarded;
    }

    @JsonProperty("lowest_id")
    public long getSinceId() {
        return sinceId;
    }
}
