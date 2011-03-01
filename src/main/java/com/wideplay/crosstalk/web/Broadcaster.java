package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.RoomStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

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
    for (Key<User> user : ImmutableList.copyOf(room.getOccupancy().getUsers())) {
      if (null != author && user.getName().equals(author.getUsername()))
        continue;

      Collection<String> channelIds = roomStore.channelOf(user, room);
      log.info("Broadcasting packet to {} [{}]\n", user.getName() + "/" + channelIds, json);

      if (null != channelIds) {
        for (String id : channelIds) {
          log.info("Sending packet to {} [{}]\n", user.getName() + "/" + id, json);
          try {
            channel.sendMessage(new ChannelMessage(id, json));
          } catch (Exception e) {
            log.error("Encountered exception during broadcast.", e);

            // Evict user (probably a stale channel id)
            log.info("Evicing user {}", user.getName());
            roomStore.leaveRoom(user, room, id);
          }
        }
      } else {
        log.info("Stale occupancy detected, evicing user {}", user.getName());
        // stale occupancy, remove from room...
//        roomStore.leaveRoom(user, room);
      }
    }
  }

}
