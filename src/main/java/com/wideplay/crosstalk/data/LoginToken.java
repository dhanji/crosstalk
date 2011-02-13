package com.wideplay.crosstalk.data;

import com.googlecode.objectify.annotation.Unindexed;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity @Unindexed
public class LoginToken {
  @Id
  private String requestToken;
  private String tokenSecret;

  public LoginToken() {
  }

  public LoginToken(String requestToken, String tokenSecret) {
    this.requestToken = requestToken;
    this.tokenSecret = tokenSecret;
  }

  public String getRequestToken() {
    return requestToken;
  }

  public String getTokenSecret() {
    return tokenSecret;
  }
}
