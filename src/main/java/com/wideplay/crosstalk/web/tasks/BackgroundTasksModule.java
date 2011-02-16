package com.wideplay.crosstalk.web.tasks;

import com.google.inject.AbstractModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class BackgroundTasksModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TaskQueue.class).asEagerSingleton();
  }
}
