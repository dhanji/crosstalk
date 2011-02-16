package com.wideplay.crosstalk.web.auth;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.crosstalk.web.CurrentUser;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class AdminMethodInterceptor implements MethodInterceptor {
  private static final ImmutableSet<String> ADMINS = ImmutableSet.of("themaninblue", "dhanji",
      "crosstalkme");
  @Inject
  private Provider<CurrentUser> currentUser;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    CurrentUser user = currentUser.get();
    if (user.isAnonymous() || !ADMINS.contains(user.getUser().getUsername())) {
      throw new IllegalAccessException("Only admin users may access this function");
    }

    return invocation.proceed();
  }
}
