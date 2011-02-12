package com.wideplay.crosstalk.web.auth;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.CurrentUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/logout") @Service
public class Logout {
  @Inject
  private Provider<CurrentUser> currentUserProvider;

  @Inject
  private Provider<UserStore> userStore;

  @Get
  Reply<?> logout(HttpServletResponse response) {
    CurrentUser currentUser = currentUserProvider.get();

    // Unset session cookie by resetting its max age.
    Cookie cookie = currentUser.getSessionCookie();
    if (null != cookie) {
      cookie.setMaxAge(0);
      response.addCookie(cookie);

      // Remove user from session store too.
      userStore.get().logout(cookie.getValue());
    }

    return Reply.saying().redirect("/r/chat/1");
  }
}
