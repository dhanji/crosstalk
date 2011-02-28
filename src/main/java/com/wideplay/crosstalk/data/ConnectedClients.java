package com.wideplay.crosstalk.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Cached;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates all connected clients. bit of a hack.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@RequestScoped
public class ConnectedClients {
  private final Objectify objectify;

  @Inject
  public ConnectedClients(Objectify objectify) {
    this.objectify = objectify;

    // Initialize myself.
    usersInRooms.addAll(objectify.query(UserRoom.class).list());
    ensure();
  }

  private Set<UserRoom> usersInRooms = Sets.newHashSet();

  // MEMO FIELD
  @Transient @JsonHide
  private Map<String, UserRoom> usersInRoomsMemo;

  public String channelOf(String userName, Room room) {
    UserRoom userRoom = usersInRoomsMemo.get(userName);
    if (null == userRoom) {
      return null;
    }

    for (RoomTokens roomToken : userRoom.roomTokens) {
      if (roomToken.roomKey.getId() == room.getId()) {
        return roomToken.token;
      }
    }

    // doesn't exist.
    return null;
  }

  public void remove(Key<User> user, Room room) {
    UserRoom userRoom = usersInRoomsMemo.get(user.getName());
    if (null == userRoom) {
      return;
    }

    RoomTokens toRemove = new RoomTokens();
    toRemove.roomKey = new Key<Room>(Room.class, room.getId());
    userRoom.roomTokens.remove(toRemove);
    objectify.put(userRoom);
  }

  @Cached @Entity
  public static class UserRoom {
    @Id
    private String username;

    @Embedded
    private Set<RoomTokens> roomTokens = Sets.newHashSet();
  }

  public static class RoomTokens {
    private Key<Room> roomKey;
    private String token;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof RoomTokens)) return false;

      RoomTokens that = (RoomTokens) o;

      if (roomKey != null ? !roomKey.equals(that.roomKey) : that.roomKey != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return roomKey != null ? roomKey.hashCode() : 0;
    }
  }

  public void add(String token, User client, Room room) {
    UserRoom userRoom = usersInRoomsMemo.get(client.getUsername());
    if (null == userRoom) {
      userRoom = new UserRoom();
      userRoom.username = client.getUsername();
      usersInRoomsMemo.put(userRoom.username, userRoom);
      usersInRooms.add(userRoom);
    }

    RoomTokens roomTokens = new RoomTokens();
    roomTokens.roomKey = new Key<Room>(Room.class, room.getId());
    roomTokens.token = token;

    userRoom.roomTokens.add(roomTokens);

    // Remember to save me!
    objectify.put(userRoom);
  }

  public Collection<Key<Room>> getRooms(User user) {
    UserRoom userRoom = usersInRoomsMemo.get(user.getUsername());
    if (null == userRoom) {
      return ImmutableList.of();
    }

    Collection<Key<Room>> rooms = Lists.newArrayList();
    for (RoomTokens roomToken : userRoom.roomTokens) {
      rooms.add(roomToken.roomKey);
    }

    return rooms;
  }

  private void ensure() {
    if (null == usersInRoomsMemo) {
      usersInRoomsMemo = Maps.newHashMap();
      for (UserRoom usersInRoom : usersInRooms) {
        usersInRoomsMemo.put(usersInRoom.username, usersInRoom);
      }
    }
  }
}
