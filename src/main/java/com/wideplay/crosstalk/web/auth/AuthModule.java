package com.wideplay.crosstalk.web.auth;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class AuthModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Twitter.class);
  }
}
