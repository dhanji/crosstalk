package com.wideplay.crosstalk.web.tasks;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
* @author dhanji@gmail.com (Dhanji R. Prasanna)
*/
class TaskQueue {
  public TaskQueue() {
    enqueueClusterTask();
    enqueueTweetTask();
  }

  public static void enqueueClusterTask() {
    QueueFactory.getDefaultQueue().add(TaskOptions.Builder
        .withUrl("/queue/cluster")
        .method(TaskOptions.Method.GET)
        .countdownMillis(2 * 60 * 1000 /* minutes */));
  }

  public static void enqueueTweetTask() {
    QueueFactory.getDefaultQueue().add(TaskOptions.Builder
        .withUrl("/queue/tweets")
        .method(TaskOptions.Method.GET)
        .countdownMillis(30 * 1000 /* seconds */));
  }
}
