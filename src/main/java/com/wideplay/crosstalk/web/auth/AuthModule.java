package com.wideplay.crosstalk.web.auth;

import com.google.inject.AbstractModule;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class AuthModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Twitter.class);

    SecureMethodInterceptor interceptor = new SecureMethodInterceptor();
    requestInjection(interceptor);
    bindInterceptor(any(), annotatedWith(Secure.class), interceptor);
  }
}
