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
  private String lastUrl;

  public LoginToken() {
  }

  public LoginToken(String requestToken, String tokenSecret, String lastUrl) {
    this.requestToken = requestToken;
    this.tokenSecret = tokenSecret;
    this.lastUrl = lastUrl;
  }

  public String getRequestToken() {
    return requestToken;
  }

  public String getTokenSecret() {
    return tokenSecret;
  }

  public String getLastUrl() {
    return lastUrl;
  }
}
