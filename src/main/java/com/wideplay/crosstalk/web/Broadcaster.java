package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.RoomStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class Broadcaster {
  private static final Logger log = LoggerFactory.getLogger(Broadcaster.class);
  @Inject
  private RoomStore roomStore;

  @Inject
  private ChannelService channel;

  public void broadcast(Room room, User author, String json) {
    for (Key<User> user : room.getOccupancy().getUsers()) {
      if (null != author && user.getName().equals(author.getUsername()))
        continue;

      log.debug("Sending packet to {} [{}]\n", user.getName(), json);
      String channelId = roomStore.channelOf(user, room);

      if (null != channelId) {
        channel.sendMessage(new ChannelMessage(channelId, json));
      } else {
        // stale occupancy, remove from room...
        roomStore.leaveRoom(user, room);
      }
    }
  }

}
