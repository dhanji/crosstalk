package com.wideplay.crosstalk.data.buzz;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class BuzzSearch {
  private List<Buzz> items;

  public List<Buzz> getItems() {
    return items;
  }

  public static class Data {
    private BuzzSearch data;

    public BuzzSearch getData() {
      return data;
    }
  }

  public static class Buzz {
    private Actor actor;

    public Actor getActor() {
      return actor;
    }
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
