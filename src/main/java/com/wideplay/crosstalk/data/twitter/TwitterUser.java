package com.wideplay.crosstalk.data.twitter;

import com.google.gson.annotations.SerializedName;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TwitterUser {
  private String location;

  @SerializedName("profile_image_url")
  private String profileImageUrl;

  @SerializedName("screen_name")
  private String screenName;

  private String name;

  public String getLocation() {
    return location;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public String getScreenName() {
    return screenName;
  }

  public String getName() {
    return name;
  }
}
