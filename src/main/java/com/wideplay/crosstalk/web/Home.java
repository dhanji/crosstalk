package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.users.UserService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.MessageStore;
import com.wideplay.crosstalk.data.Occupancy;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/chat")
public class Home {
  @Inject
  private UserService userService;

  @Inject
  private ChannelService channelService;

  @Inject
  private AsyncPostService.ConnectedClients clients;

  @Inject
  private MessageStore store;

  private String token;

  @Get
  void get() {
    // Create channel token specific to this user.
    User user = getUser();
    token = channelService.createChannel(user.getUsername());
    clients.add(token, user);

    // Set up presence for this room.
    Occupancy occupancy = Room.DEFAULT.getOccupancy();
    occupancy.getUsers().add(user);

//    QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/queue/hashtag"));
  }

  public String getCometToken() {
    return token;
  }

  public User getUser() {
    return User.named(userService);
  }

  public Room getRoom() {
    return Room.DEFAULT;
  }

  public Collection<String> getOccupants() {
    Set<User> users = Room.DEFAULT.getOccupancy().getUsers();
    List<String> usernames = Lists.newArrayListWithCapacity(users.size());
    for (User user : users) {
      usernames.add(user.getUsername());
    }
    return usernames;
  }
}
