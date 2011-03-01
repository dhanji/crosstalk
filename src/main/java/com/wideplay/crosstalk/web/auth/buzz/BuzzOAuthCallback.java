package com.wideplay.crosstalk.web.auth.buzz;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.LoginToken;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.buzz.BuzzSearch;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/oauth/buzz") @Service
public class BuzzOAuthCallback {
  private static final Logger log = LoggerFactory.getLogger(BuzzOAuthCallback.class);

  @Inject
  private Provider<CurrentUser> currentUser;

  @Inject
  private Provider<UserStore> userStore;

  @Inject
  private Gson gson;

  @Inject
  private BuzzApi buzz;

  @Get
  Reply<?> callback(Request request, HttpServletResponse response) {
    String token = request.param("oauth_token");
    String verifier = request.param("oauth_verifier");

    String redirect = "/r/chat/1";
    log.debug("Twitter callback successful with verifier {} ", verifier);
    if (verifier != null) {
      LoginToken loginToken = buzz.authorize(token, verifier);
      if (loginToken.getLastUrl() != null) {
        redirect = loginToken.getLastUrl();
      }
    }

    // And now we should log this user in properly.
    CurrentUser thisUser = currentUser.get();

    // Set session cookie.
    String sessionId = thisUser.newSessionId();
    Cookie cookie = new Cookie(CurrentUser.SESSION_COOKIE_NAME, sessionId);
    cookie.setPath("/");
    cookie.setMaxAge(60 * 60 * 24 /* 1 day */);
    response.addCookie(cookie);

    // We first need to some how get the username out of this so we can identify who it is!
    String creds = buzz.call("https://www.googleapis.com/buzz/v1/activities/@me/@self?alt=json");

    // Log user in, in our own user store.
    BuzzSearch data = gson.fromJson(creds, BuzzSearch.Data.class).getData();

    User user = new User();
    if (data.getItems().isEmpty()) {
      return Reply.with("Error: You have never posted anything in Buzz so we can't log you in!");
      // Maybe check something else?
//      creds = buzz.call("https://www.googleapis.com/buzz/v1/people/@me/@self?alt=json");
//      BuzzUser userData = gson.fromJson(creds, BuzzUser.Data.class).getUser();
//      user.setUsername(userData.getDisplayName());
//      user.setDisplayName(userData.getDisplayName());
//      user.setAvatar(userData.getAvatar());
    } else {
      BuzzSearch.Buzz buzz = data.getItems().get(0);
      if (!buzz.getArbitraryPermalink().startsWith("http://www.google.com/buzz/a/google.com/")) {
        // Dont allow non-google domains.
        return Reply.saying().forbidden();
      }
      user.setUsername(buzz.getActor().getName());
      user.setDisplayName(buzz.getActor().getName());
      user.setAvatar(buzz.getActor().getAvatar());
    }

    user.setTwitterAccessToken(thisUser.getUser().getTwitterAccessToken());
    user.setTwitterTokenSecret(thisUser.getUser().getTwitterTokenSecret());

    userStore.get().loginAndMaybeCreate(sessionId, user);

    return Reply.saying().redirect(redirect) ;
  }
}
