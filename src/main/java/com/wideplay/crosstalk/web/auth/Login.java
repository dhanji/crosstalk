package com.wideplay.crosstalk.web.auth;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.CurrentUser;
import com.wideplay.crosstalk.web.auth.Twitter.OAuthRedirect;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/login") @Service
public class Login {
  @Inject
  private Provider<CurrentUser> currentUser;

  @Inject Provider<UserStore> userStore;

  @Get
  Reply<?> get(Twitter twitter) {
    OAuthRedirect redirect = twitter.redirectForAuth();

    // We need to save these temporary credentials to complete the OAuth dance.
    userStore.get().newOAuthToken(redirect.getRequestToken(), redirect.getTokenSecret());

    return Reply.saying().redirect(redirect.getUrl());
  }
}
