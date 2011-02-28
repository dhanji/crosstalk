package com.wideplay.crosstalk.web.auth;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.Broadcaster;
import com.wideplay.crosstalk.web.CurrentUser;
import com.wideplay.crosstalk.web.auth.buzz.BuzzApi;
import com.wideplay.crosstalk.web.auth.twitter.Twitter;
import com.wideplay.crosstalk.web.auth.twitter.Twitter.OAuthRedirect;
import com.wideplay.crosstalk.web.auth.twitter.TwitterMode;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/login") @Service
public class Login {
  @Inject
  private Provider<CurrentUser> currentUser;

  @Inject
  private UserStore userStore;

  @Inject
  private RoomStore roomStore;

  @Inject
  private Broadcaster broadcaster;

  @Inject
  private Twitter twitter;

  @Inject
  private BuzzApi buzz;

  @Get
  Reply<?> get(@TwitterMode boolean twitterMode, Request request, Gson gson) {
    // Decrement lurker count.
    String roomId = request.param("r");
    String lastUrl = request.param("u");
    CurrentUser user = currentUser.get();
    if (null != roomId && user.isAnonymous()) {
      Room room = roomStore.byId(Long.valueOf(roomId));
      if (null != room) {
        // Broadcast that one anonymous user has left...
        broadcaster.broadcast(room, null, gson.toJson(ImmutableMap.of(
            "rpc", "leave",
            "leaver", user.getUser()
        )));
      }
    }

    String redirectUrl;
    if (twitterMode) {
      OAuthRedirect redirect = twitter.redirectForAuth();
      redirectUrl = redirect.getUrl();

      // We need to save these temporary credentials to complete the OAuth dance.
      userStore.newOAuthToken(redirect.getRequestToken(), redirect.getTokenSecret(), lastUrl);
    } else {
      OAuthRedirect redirect = buzz.redirectForAuth();
      redirectUrl = redirect.getUrl();

      // We need to save these temporary credentials to complete the OAuth dance.
      userStore.newOAuthToken(redirect.getRequestToken(), redirect.getTokenSecret(), lastUrl);
    }

    return Reply.saying().redirect(redirectUrl);
  }
}
