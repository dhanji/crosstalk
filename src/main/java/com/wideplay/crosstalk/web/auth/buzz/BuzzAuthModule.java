package com.wideplay.crosstalk.web.auth.buzz;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import com.wideplay.crosstalk.web.auth.twitter.TwitterMode;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class BuzzAuthModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ServletModule() {

      @Override
      protected void configureServlets() {
        filter("/r/*").through(BuzzAuthFilter.class);
        filter("/logout").through(BuzzAuthFilter.class);
        filter("/oauth/buzz").through(BuzzAuthFilter.class); // HACK!
      }

    });
    bindConstant().annotatedWith(TwitterMode.class).to(false);
    bind(BuzzApi.class);
  }
}
