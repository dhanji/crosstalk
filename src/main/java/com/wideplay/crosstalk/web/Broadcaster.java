package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class Broadcaster {
  private static final Logger log = LoggerFactory.getLogger(Broadcaster.class);
  @Inject
  private AsyncPostService.ConnectedClients connected;

  @Inject
  private ChannelService channel;

  public void broadcast(Room room, User author, String json) {
    for (Key<User> user : room.getOccupancy().getUsers()) {
      if (null != author && user.getName().equals(author.getUsername()))
        continue;

      log.debug("Sending packet to {} [{}]\n", user.getName(), json);
      String channelId = connected.clients.get(user.getName()).get(room);
      if (null != channelId) {
        channel.sendMessage(new ChannelMessage(channelId, json));
      } else {
        // stale occupancy, update...

      }
    }
  }

}
