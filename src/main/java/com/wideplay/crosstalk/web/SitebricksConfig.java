package com.wideplay.crosstalk.web;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.wideplay.crosstalk.CrosstalkModule;
import com.wideplay.crosstalk.web.auth.buzz.BuzzAuthModule;
import com.wideplay.crosstalk.web.auth.twitter.TwitterAuthModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SitebricksConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        String twittermode = System.getProperty("twittermode");
        if (null != twittermode && Boolean.valueOf(twittermode)) {
          install(new TwitterAuthModule());
        } else {
          install(new BuzzAuthModule());
        }

        install(new CrosstalkModule());
      }
    });
  }
}
