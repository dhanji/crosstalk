package com.wideplay.crosstalk.web.auth;

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
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.data.twitter.TwitterUser;
import com.wideplay.crosstalk.web.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/oauth/twitter") @Service
public class TwitterOAuthCallback {
  private static final Logger log = LoggerFactory.getLogger(TwitterOAuthCallback.class);

  @Inject
  private Provider<CurrentUser> currentUser;

  @Inject
  private Provider<UserStore> userStore;

  @Inject
  private Gson gson;

  @Get
  Reply<?> callback(Twitter twitter, Request request, HttpServletResponse response) {
    String token = request.param("oauth_token");
    String verifier = request.param("oauth_verifier");

    String redirect = "/r/chat/1";
    log.debug("Twitter callback successful with verifier {} ", verifier);
    if (verifier != null) {
      LoginToken loginToken = twitter.authorize(token, verifier);
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
    String creds = twitter.call("http://api.twitter.com/1/account/verify_credentials.json");


    // Log user in, in our own user store.
    TwitterUser twitterUser = gson.fromJson(creds, TwitterUser.class);
    User user = new User();
    user.setUsername(twitterUser.getScreenName());
    user.setDisplayName(twitterUser.getName());
    user.setAvatar(twitterUser.getProfileImageUrl());

    userStore.get().loginAndMaybeCreate(sessionId, user);

    return Reply.saying().redirect(redirect) ;
  }
}
