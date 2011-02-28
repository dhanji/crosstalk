package com.wideplay.crosstalk.web.auth;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;

import static com.google.inject.matcher.Matchers.any;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class AuthModule extends AbstractModule {
  @Override
  protected void configure() {
    MethodInterceptor interceptor = new SecureMethodInterceptor();
    requestInjection(interceptor);
    bindInterceptor(any(), Matchers.annotatedWith(Secure.class), interceptor);

    interceptor = new AdminMethodInterceptor();
    requestInjection(interceptor);
    bindInterceptor(any(), Matchers.annotatedWith(AdminOnly.class), interceptor);
  }
}
