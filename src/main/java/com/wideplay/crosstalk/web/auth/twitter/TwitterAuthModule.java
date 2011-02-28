package com.wideplay.crosstalk.web.auth.twitter;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TwitterAuthModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ServletModule() {

      @Override
      protected void configureServlets() {
        filter("/r/*").through(TwitterAuthFilter.class);
        filter("/logout").through(TwitterAuthFilter.class);
        filter("/oauth/twitter").through(TwitterAuthFilter.class); // HACK!
      }

    });
    bindConstant().annotatedWith(TwitterMode.class).to(true);
  }
}
