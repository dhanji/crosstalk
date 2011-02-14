package com.wideplay.crosstalk.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.sitebricks.SitebricksModule;
import com.wideplay.crosstalk.CrosstalkModule;
import com.wideplay.crosstalk.web.auth.AuthFilter;
import com.wideplay.crosstalk.web.auth.AuthModule;
import com.wideplay.crosstalk.web.tasks.BackgroundTasksModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SitebricksConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new ServletModule() {

      @Override
      protected void configureServlets() {
        filter("/r/*").through(AuthFilter.class);
        filter("/oauth/twitter").through(AuthFilter.class); // HACK!
      }

    }, new SitebricksModule() {

      @Override
      protected void configureSitebricks() {
        scan(SitebricksConfig.class.getPackage());

        install(new CrosstalkModule());
        install(new AuthModule());
        install(new BackgroundTasksModule());
      }

    });
  }
}
