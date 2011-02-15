package com.wideplay.crosstalk.web;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.wideplay.crosstalk.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Represents the current user for this request.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@RequestScoped
public class CurrentUser {
  private static final Logger log = LoggerFactory.getLogger(CurrentUser.class);
  public static final String SESSION_COOKIE_NAME = "x-crosstalk-session-id";

  @Inject
  private HttpServletRequest request;

  private User user;

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String newSessionId() {
    return UUID.randomUUID().toString();
  }

  public boolean isAnonymous() {
    return user != null && User.ANONYMOUS.equals(user);
  }

  public Cookie getSessionCookie() {
    Cookie[] cookies = request.getCookies();
    if (null == cookies) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
        return cookie;
      }
    }

    return null;
  }
}
