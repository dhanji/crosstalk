package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.users.UserService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Occupancy;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.MessageStore;

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

  @Inject
  private CurrentUser currentUser;

  private String token;

  @Get
  void get() {
    // Create channel token specific to this user.
    User user = getUser();

    // TODO(dhanji): Support multiple rooms at once per user.
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
    return currentUser.getUser();
  }

  public Room getRoom() {
    return Room.DEFAULT;
  }

  public Collection<User> getOccupants() {
    Set<User> users = Sets.newHashSet(Room.DEFAULT.getOccupancy().getUsers());
    users.remove(User.ANONYMOUS);
    
    return users;
  }

  // For current room.
  public List<Message> getMessages() {
    return ImmutableList.of();  
  }
}
