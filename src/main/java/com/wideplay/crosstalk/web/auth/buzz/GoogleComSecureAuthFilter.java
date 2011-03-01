package com.wideplay.crosstalk.web.auth.buzz;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.wideplay.crosstalk.web.CurrentUser;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class GoogleComSecureAuthFilter implements Filter {
  @Inject
  private Provider<CurrentUser> currentUser;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    // Don't allow the anonymous user to get in to this site.
    if (currentUser.get().isAnonymous()) {
      ((HttpServletResponse)response).sendRedirect("/login");
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}
