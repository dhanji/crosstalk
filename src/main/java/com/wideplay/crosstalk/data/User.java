package com.wideplay.crosstalk.data;

import com.google.appengine.api.users.UserService;

import java.util.Date;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class User {
  private String username; // twitter username (unique id), hmm...
  private String displayName;
  private Date createdOn;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;

    User user = (User) o;

    if (username != null ? !username.equals(user.username) : user.username != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return username != null ? username.hashCode() : 0;
  }

  public static User named(UserService userService) {
    User user = new User();
    user.setUsername(userService.getCurrentUser().getNickname());
    user.setDisplayName(user.getUsername());
    return user;
  }
}
