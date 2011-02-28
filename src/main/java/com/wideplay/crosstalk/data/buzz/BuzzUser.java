package com.wideplay.crosstalk.data.buzz;

import com.google.gson.annotations.SerializedName;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class BuzzUser {
  @SerializedName("thumbnailUrl")
  private String avatar;

  private String displayName;

  public String getAvatar() {
    return avatar;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static class Data {
    @SerializedName("data")
    private BuzzUser user;

    public BuzzUser getUser() {
      return user;
    }
  }
}
