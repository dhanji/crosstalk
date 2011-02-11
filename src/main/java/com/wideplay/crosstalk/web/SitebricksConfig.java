package com.wideplay.crosstalk.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.sitebricks.SitebricksModule;
import com.wideplay.crosstalk.CrosstalkModule;

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
      }

    }, new SitebricksModule() {

      @Override
      protected void configureSitebricks() {
        scan(SitebricksConfig.class.getPackage());

        install(new CrosstalkModule());
      }

    });
  }
}
