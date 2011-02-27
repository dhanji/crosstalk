package com.wideplay.crosstalk.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.objectify.Key;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
* @author dhanji@gmail.com (Dhanji R. Prasanna)
*/
@Entity
public class ConnectedClients {
  public static final long SINGLETON_ID = 1L;

  @Id
  private long id = SINGLETON_ID;

  @Embedded
  private Set<UserRoom> usersInRooms = Sets.newHashSet();

  // MEMO FIELD
  @Transient @JsonHide
  private Map<String, UserRoom> usersInRoomsMemo;

  public String channelOf(String userName, Room room) {
    ensure();
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
    ensure();

    UserRoom userRoom = usersInRoomsMemo.get(user.getName());
    if (null == userRoom) {
      return;
    }

    RoomTokens toRemove = new RoomTokens();
    toRemove.roomKey = new Key<Room>(Room.class, room.getId());
    userRoom.roomTokens.remove(toRemove);
  }

  public static class UserRoom {
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
    ensure();

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
  }

  public Collection<Key<Room>> getRooms(User user) {
    ensure();

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
