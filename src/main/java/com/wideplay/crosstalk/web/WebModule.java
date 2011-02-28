package com.wideplay.crosstalk.web;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.RequestScoped;
import com.wideplay.crosstalk.web.auth.twitter.Twitter;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class WebModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(CurrentUser.class).in(RequestScoped.class);
    bind(Twitter.class);
  }
}
