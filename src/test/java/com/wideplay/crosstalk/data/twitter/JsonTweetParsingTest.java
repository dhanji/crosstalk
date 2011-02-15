package com.wideplay.crosstalk.data.twitter;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class JsonTweetParsingTest {

  @Test
  public final void parse() throws IOException {
    String json = IOUtils.toString(TwitterSearch.class.getResourceAsStream("example_tweet_search.json"));

    TwitterSearch tweets = new Gson().fromJson(json, TwitterSearch.class);

    System.out.println("Parsed: " + tweets.getResults());
  }
}
