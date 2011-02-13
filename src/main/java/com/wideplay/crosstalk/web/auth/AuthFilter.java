package com.wideplay.crosstalk.web.auth;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import java.io.IOException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class AuthFilter implements Filter {
  private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

  @Inject
  private Provider<UserStore> userStoreProvider;

  @Inject
  private Provider<CurrentUser> currentUserProvider;

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void doFilter(ServletRequest servletRequest,
                       ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    UserStore userStore = userStoreProvider.get();
    CurrentUser currentUser = currentUserProvider.get();

    // First see if there is a session cookie.
    Cookie sessionCookie = currentUser.getSessionCookie();
    if (null == sessionCookie) {
      // Auth as anonymous.
      currentUser.setUser(User.ANONYMOUS);
    } else {
      // Find the user associated with this session cookie and log her in.
      User loggedIn = userStore.isLoggedIn(sessionCookie.getValue());

      // No such user was found. (Invalid session cookie, continue as anonymous)
      if (null == loggedIn) {
        loggedIn = User.ANONYMOUS;
      }

      currentUser.setUser(loggedIn);
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  public void destroy() {
  }
}
