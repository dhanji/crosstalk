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

        // Secures site only to google.com users by preventing access unless logged in.
        if (Boolean.valueOf(System.getProperty("supersecure"))) {
          filter("/r/*").through(GoogleComSecureAuthFilter.class);
          filter("/").through(GoogleComSecureAuthFilter.class);
          filter("/r/room_admin").through(GoogleComSecureAuthFilter.class);
        }
      }

    });
    bindConstant().annotatedWith(TwitterMode.class).to(false);
    bind(BuzzApi.class);
  }
}
