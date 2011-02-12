package com.wideplay.crosstalk.data.store;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Room;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStore {
  static {
    // All managed data types.
    ObjectifyService.register(Room.class);
    ObjectifyService.register(Message.class);
  }

  @Inject
  private Objectify objectify;

  public void save(Message message) {
  }
}
