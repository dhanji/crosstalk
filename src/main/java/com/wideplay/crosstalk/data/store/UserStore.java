package com.wideplay.crosstalk.data.store;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.wideplay.crosstalk.data.User;

import java.util.concurrent.ConcurrentMap;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class UserStore {
  static {
    // All managed data types.
    ObjectifyService.register(User.class);
  }

  private final ConcurrentMap<String, User> loggedInUsers = new MapMaker().makeMap();
  private final ConcurrentMap<String, String> unclaimedOAuthTokens = new MapMaker().makeMap();

  @Inject
  private Objectify objectify;

  /**
   * Returns user if logged in, or null.
   */
  public User isLoggedIn(String sessionId) {
    System.out.println("Getting: " + loggedInUsers);

    // TODO look in data store if not present in local cache.
    return loggedInUsers.get(sessionId);
  }

  public void logout(String sessionId) {
    // TODO update in data store.
    loggedInUsers.remove(sessionId);
  }

  public String claimOAuthToken(String requestToken) {
    return unclaimedOAuthTokens.remove(requestToken);
  }

  public String newOAuthToken(String requestToken, String tokenSecret) {
    return unclaimedOAuthTokens.put(requestToken, tokenSecret);
  }

  public void loginAndMaybeCreate(String sessionId, User user) {
    loggedInUsers.put(sessionId, user);
    System.out.println("Stashing: " + loggedInUsers);
  }
}
