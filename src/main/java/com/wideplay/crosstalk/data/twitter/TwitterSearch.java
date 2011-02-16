package com.wideplay.crosstalk.data.twitter;

import com.google.gson.annotations.SerializedName;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.User;

import java.util.Date;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TwitterSearch {
  private List<Tweet> results;

  public List<Tweet> getResults() {
    return results;
  }

  public Message pick() {
    if (results.isEmpty()) {
      return null;
    }

    // Pick a random tweet.
    Tweet tweet = results.get((int)((Math.random() * results.size()) % results.size()));

    Message message = new Message();
    message.setText(tweet.text);
    message.setPostedOn(new Date()); // set properly
    message.setTweet(true);

    User author = new User();
    author.setAvatar(tweet.avatar);
    author.setUsername(tweet.username);
    author.setDisplayName(tweet.username);
    message.setAuthor(author);

    return message;
  }

  public static class Tweet {
    @SerializedName("from_user")
    private String username;

    @SerializedName("profile_image_url")
    private String avatar;

    @SerializedName("created_at")
    private String postedOn;

    private String text;

    public String getUsername() {
      return username;
    }

    public String getAvatar() {
      return avatar;
    }

    public String getPostedOn() {
      return postedOn;
    }

    public String getText() {
      return text;
    }

    @Override
    public String toString() {
      return "Tweet{" +
          "username='" + username + '\'' +
          ", avatar='" + avatar + '\'' +
          ", postedOn='" + postedOn + '\'' +
          ", text='" + text + '\'' +
          '}';
    }
  }
}
