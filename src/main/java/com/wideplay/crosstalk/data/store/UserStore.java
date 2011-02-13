package com.wideplay.crosstalk.data.store;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Objectify;
import com.wideplay.crosstalk.data.User;

import java.util.concurrent.ConcurrentMap;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class UserStore {
  // TODO re-activate these hot-caches, and maybe back with memcache too.
  private final ConcurrentMap<String, User> loggedInUsers = new MapMaker().makeMap();
  private final ConcurrentMap<String, String> unclaimedOAuthTokens = new MapMaker().makeMap();

  @Inject
  private Objectify objectify;

  /**
   * Returns user if logged in, or null.
   */
  public User isLoggedIn(String sessionId) {
    return objectify.query(User.class).filter("sessionId", sessionId).get();
  }

  public void logout(String sessionId) {
    User user = isLoggedIn(sessionId);
    if (null != user) {
      user.setSessionId(null);
      objectify.put(user);
    }
  }

  public void loginAndMaybeCreate(String sessionId, User user) {
    User found = objectify.find(User.class, user.getUsername());
    if (found == null) {
      found = user;
    }

    found.setSessionId(sessionId);
    objectify.put(found);
  }

  public String claimOAuthToken(String requestToken) {
    LoginToken token = objectify.find(LoginToken.class, requestToken);
    if (null == token) {
      return null;
    }
    objectify.delete(LoginToken.class, requestToken);
    return token.getTokenSecret();
  }

  public void newOAuthToken(String requestToken, String tokenSecret) {
    objectify.put(new LoginToken(requestToken, tokenSecret));
  }
}
