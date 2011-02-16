package com.wideplay.crosstalk.web.auth;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.crosstalk.web.CurrentUser;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SecureMethodInterceptor implements MethodInterceptor {
  @Inject
  private Provider<CurrentUser> currentUser;
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (currentUser.get().isAnonymous()) {
      throw new IllegalAccessException("Anonymous users may not access this function");
    }

    return invocation.proceed();
  }
}
