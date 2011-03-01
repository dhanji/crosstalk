package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.CrosstalkModule;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Occupancy;
import com.wideplay.crosstalk.data.Occupancy.TimeSegment;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.MessageStore;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.auth.twitter.TwitterMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/chat/:room")
public class RoomPage {
  public static final double MAX_ACTIVITY_BUBBLES = 6.0;
  @Inject
  private ChannelService channelService;

  @Inject
  private CurrentUser currentUser;

  @Inject
  private RoomStore roomStore;

  @Inject
  private MessageStore messageStore;

  @Inject
  private UserStore userStore;

  @Inject @TwitterMode
  private boolean twitterMode;

  private String token;
  private Room room;
  private List<Message> messages;
  private int maxActivity;

  // Not thread safe, so need to create it each turn.
  private final DateFormat messageDateFormat = new SimpleDateFormat(CrosstalkModule.POST_DATE_FORMAT);

  @Get
  String displayRoom(@Named("room") String roomId) {
    if (roomId == null || roomId.isEmpty()) {
      return "/";
    }

    // Find the room with this id.
    room = roomStore.named(roomId);

    // No such room!
    if (null == room) {
      return "/";
    }

    // Create channel token specific to this user.
    User user = getUser();

    // Create a room & user-specific unique id for this channel.
    String userChannelId = user.getUsername() + roomId + UUID.randomUUID().toString();
    token = channelService.createChannel(userChannelId);
    roomStore.connectClient(user, userChannelId, room);

    // Set up presence for this room.
    Occupancy occupancy = room.getOccupancy();
    occupancy.add(user);

    // Update occupancy.
    roomStore.save(occupancy);

    // Load the initial set of messages (currently loads everything).
    messages = messageStore.list(room);

    return null;
  }

  public String getCometToken() {
    return token;
  }

  public User getUser() {
    return currentUser.getUser();
  }

  public Room getRoom() {
    return room;
  }

  public Collection<User> getOccupants() {
    Collection<User> users = userStore.resolve(room.getOccupancy().getUsers()).values();
    users.remove(User.anonymous());
    return users;
  }

  // For current room.
  public List<Message> getMessages() {
    return messages;
  }

  public String format(Date date) {
    return messageDateFormat.format(date);
  }

  public String activity(TimeSegment segment) {
    return renderActivity(getRoom(), segment);
  }

  public static String renderActivity(Room room, TimeSegment segment) {
    // Show a max of MAX_ACTIVITY_BUBBLES bubbles.
    double ratio = segment.getCount() / ((double) room.getOccupancy().getMaxActivity());

    int count = (int)(ratio * MAX_ACTIVITY_BUBBLES);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < count; i++) {
      builder.append("<div class='bubble'></div>");
    }
    return builder.toString();
  }

  public boolean getTwitterMode() {
    return twitterMode;
  }
  public boolean isTwitterMode() {
    return twitterMode;
  }
}
