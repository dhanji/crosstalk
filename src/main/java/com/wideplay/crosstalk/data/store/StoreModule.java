package com.wideplay.crosstalk.data.store;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.wideplay.crosstalk.data.*;
import com.wideplay.crosstalk.data.RoomTextIndex;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class StoreModule extends AbstractModule {
  static {
    ObjectifyService.register(Room.class);
    ObjectifyService.register(Occupancy.class);
    ObjectifyService.register(User.class);
    ObjectifyService.register(Message.class);
    ObjectifyService.register(Attachment.class);
    ObjectifyService.register(LoginToken.class);
    ObjectifyService.register(RoomTextIndex.class);
    ObjectifyService.register(ConnectedClients.UserRoom.class);
  }

  @Override
  protected void configure() {
  }

  @Provides
  @RequestScoped
  Objectify provideObjectify() {
    return ObjectifyService.begin();
  }
}
