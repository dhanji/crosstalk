package com.wideplay.crosstalk.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Serialized;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates all connected clients. bit of a hack.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
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
  private Map<String, UserRoom> usersInRoomsMemo;

  public Collection<String> channelOf(String userName, Room room) {
    UserRoom userRoom = usersInRoomsMemo.get(userName);
    if (null == userRoom) {
      return null;
    }

    for (RoomTokens roomToken : userRoom.roomTokens) {
      if (roomToken.roomKey.getId() == room.getId()) {
        return roomToken.tokens;
      }
    }

    // doesn't exist.
    return null;
  }

  public boolean remove(Key<User> user, Room room, String id) {
    UserRoom userRoom = usersInRoomsMemo.get(user.getName());
    if (null == userRoom) {
      return true;
    }

    boolean leftRoom = false;
    for (RoomTokens roomToken : userRoom.roomTokens) {
      if (roomToken.roomKey.getId() == room.getId()) {
        roomToken.tokens.remove(id);
        leftRoom = roomToken.tokens.isEmpty();
        break;
      }
    }
    objectify.put(userRoom);

    return leftRoom;
  }

  public void removeAll(Key<User> user, Room room) {
    UserRoom userRoom = usersInRoomsMemo.get(user.getName());
    if (null == userRoom) {
      return;
    }

    for (RoomTokens roomToken : userRoom.roomTokens) {
      if (roomToken.roomKey.getId() == room.getId()) {
        roomToken.tokens.clear();
        break;
      }
    }
    objectify.put(userRoom);
  }

  @Cached @Entity
  public static class UserRoom {
    @Id
    private String username;

    @Serialized
    private Set<RoomTokens> roomTokens = Sets.newHashSet();
  }

  public static class RoomTokens implements Serializable {
    private static final long serialVerionUID = -1L;
    private Key<Room> roomKey;
    private Set<String> tokens = Sets.newHashSet();

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

    RoomTokens found = null;
    for (RoomTokens roomToken : userRoom.roomTokens) {
      if (roomToken.roomKey.getId() == room.getId()) {
        found = roomToken;
        break;
      }
    }

    // Should we create a new one?
    if (null == found) {
      found = new RoomTokens();
      found.roomKey = new Key<Room>(Room.class, room.getId());
      userRoom.roomTokens.add(found);
    }
    found.tokens.add(token);

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
