package com.wideplay.crosstalk.web;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class AuthFilter implements Filter {
  @Inject
  private Provider<UserService> userService;

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void doFilter(ServletRequest servletRequest,
                       ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    UserService service = userService.get();
    if (service.isUserLoggedIn()) {
      filterChain.doFilter(servletRequest, servletResponse);
    } else {
      // redirect to login page!
      String uri = ((HttpServletRequest) servletRequest).getRequestURI();
      ((HttpServletResponse)servletResponse).sendRedirect(service.createLoginURL(uri));
    }
  }

  public void destroy() {
  }
}
