package com.wideplay.crosstalk.web.auth;

import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.web.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/cookie") @Service
public class CookieTest {
  private static final Logger log = LoggerFactory.getLogger(CookieTest.class);

  @Inject
  CurrentUser currentUser;

  @Get
  Reply<?> callback(HttpServletRequest request, HttpServletResponse response) {
    StringBuilder builder = new StringBuilder();
    for (Cookie cookie : request.getCookies()) {
      builder.append(cookie.getName());
      builder.append(" : ");
      builder.append(cookie.getValue());
      builder.append(" : ");
      builder.append(cookie.getVersion());
    }

    // Set session cookie.
    response.addCookie(new Cookie(CurrentUser.SESSION_COOKIE_NAME, UUID.randomUUID().toString()));

    return Reply.with("Set!\n\n\n" + builder + "<br/>" + currentUser.getSessionCookie().getValue());
  }
}
