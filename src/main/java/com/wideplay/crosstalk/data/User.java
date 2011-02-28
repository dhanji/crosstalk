package com.wideplay.crosstalk.data;

import com.google.appengine.api.users.UserService;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Unindexed;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity
public class User {
  @Id
  private String username; // twitter username (unique id), hmm...
  private String displayName;
  private Date createdOn;

  @Unindexed
  private String avatar; // URL

  @Unindexed @JsonHide
  private String twitterAccessToken;

  @Unindexed @JsonHide
  private String twitterTokenSecret;

  @JsonHide
  private String sessionId;

  // Set up the anonymous user (special case)
  public static final String ANONYMOUS_USERNAME = "anonymous";
  public static transient final Key<User> ANONYMOUS_KEY = new Key<User>(User.class,
      ANONYMOUS_USERNAME);

  // A reusable, mutable anonymous user.
  public static User anonymous() {
    User user = new User();
    user.setUsername(ANONYMOUS_USERNAME);
    user.setAvatar("");
    user.setCreatedOn(new Date(0));
    user.setDisplayName("Lurker");
    return user;
  }

  public boolean isGhost() {
    return twitterAccessToken == null;
  }

  public String getTwitterAccessToken() {
    return twitterAccessToken;
  }

  public void setTwitterAccessToken(String twitterAccessToken) {
    this.twitterAccessToken = twitterAccessToken;
  }

  public String getTwitterTokenSecret() {
    return twitterTokenSecret;
  }

  public void setTwitterTokenSecret(String twitterTokenSecret) {
    this.twitterTokenSecret = twitterTokenSecret;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

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

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
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

  @Override
  public String toString() {
    return "<" + username + ">";
  }
}
