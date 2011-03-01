package com.wideplay.crosstalk.data.twitter;

import com.google.gson.Gson;
import com.wideplay.crosstalk.data.buzz.BuzzSearch;
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


  @Test
  public final void parseBuzz() throws IOException {
    String json = IOUtils.toString(TwitterSearch.class.getResourceAsStream("example_buzz_@me.json"));

    BuzzSearch tweets = new Gson().fromJson(json, BuzzSearch.Data.class).getData();

    System.out.println("Parsed: " + tweets.getItems());
  }


}
