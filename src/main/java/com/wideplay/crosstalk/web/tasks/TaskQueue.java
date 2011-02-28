package com.wideplay.crosstalk.web.tasks;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;
import com.wideplay.crosstalk.web.auth.twitter.TwitterMode;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class TaskQueue {
  @Inject
  public TaskQueue(@TwitterMode boolean twitterMode) {
    if (twitterMode) {
      enqueueTweetTask();
    }
    enqueueClusterTask();
  }

  public static void enqueueClusterTask() {
    QueueFactory.getDefaultQueue().add(TaskOptions.Builder
        .withUrl("/queue/cluster")
        .method(TaskOptions.Method.GET)
        .countdownMillis(60 * 1000 /* 1/2 hour */));
  }

  public static void enqueueTweetTask() {
    QueueFactory.getDefaultQueue().add(TaskOptions.Builder
        .withUrl("/queue/tweets")
        .method(TaskOptions.Method.GET)
        .countdownMillis(4 * 60 * 1000 /* minutes */));
  }
}
