package com.wideplay.crosstalk.data.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.wideplay.crosstalk.data.LoginToken;
import com.wideplay.crosstalk.data.User;

import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class UserStore {

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

  public Map<Key<User>, User> resolve(Set<Key<User>> users) {
    return objectify.get(users);
  }

  public LoginToken claimOAuthToken(String requestToken) {
    LoginToken token = objectify.find(LoginToken.class, requestToken);
    if (null == token) {
      return null;
    }
    objectify.delete(LoginToken.class, requestToken);
    return token;
  }

  public void newOAuthToken(String requestToken, String tokenSecret, String lastUrl) {
    objectify.put(new LoginToken(requestToken, tokenSecret, lastUrl));
  }

  public User fetch(Key<User> userKey) {
    return objectify.get(userKey);
  }

  public void createGhost(User author) {
    objectify.put(author);
  }
}
