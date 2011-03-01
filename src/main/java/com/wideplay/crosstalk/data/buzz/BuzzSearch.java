package com.wideplay.crosstalk.data.buzz;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.User;

import java.util.Date;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class BuzzSearch {
  private List<Buzz> items = Lists.newArrayList();

  public List<Buzz> getItems() {
    return items;
  }

  public Message pick() {
    if (items.isEmpty()) {
      return null;
    }

    // Pick a random buzz.
    Buzz buzz = items.get((int) ((Math.random() * items.size()) % items.size()));

    Message message = new Message();
    message.setId((long) buzz.id.hashCode()); // UGH HACK.
    message.setText(buzz.title);
    message.setPostedOn(new Date()); // set properly
    message.setTweet(true);

    User author = new User();
    author.setAvatar(buzz.actor.getAvatar());
    author.setUsername(buzz.actor.getName());
    author.setDisplayName(buzz.actor.getName());
    message.setAuthor(author);

    return message;
  }

  public static class Data {
    private BuzzSearch data;

    public BuzzSearch getData() {
      return data;
    }
  }

  public static class Buzz {
    private Actor actor;
    private String id;
    private String title;

    private Links links;

    public String getArbitraryPermalink() {
      return links.alternate.isEmpty() ? null : links.alternate.get(0).href;
    }

    public Actor getActor() {
      return actor;
    }

    public String getId() {
      return id;
    }
  }

  public static class Links {
    private List<Alternate> liked = Lists.newArrayList();
    private List<Alternate> alternate = Lists.newArrayList();
  }

  public static class Alternate {
    private String href;
    private String type;
    private String count;
  }

  public static class Actor {
    private String name;

    @SerializedName("thumbnailUrl")
    private String avatar;

    public String getName() {
      return name;
    }

    public String getAvatar() {
      return avatar;
    }
  }
}
