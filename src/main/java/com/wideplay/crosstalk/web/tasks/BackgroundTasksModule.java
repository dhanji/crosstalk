package com.wideplay.crosstalk.web.tasks;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.AbstractModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class BackgroundTasksModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Initializer.class).asEagerSingleton();
  }

  public static class Initializer {
    public Initializer() {
      QueueFactory.getDefaultQueue().add(TaskOptions.Builder
          .withUrl("/queue/cluster")
          .method(TaskOptions.Method.GET)
          .countdownMillis(5 * 1000)
      );
//    QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/queue/hashtag"));
    }
  }
}
