package com.wideplay.crosstalk.data.store;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.wideplay.crosstalk.data.ConnectedClients;
import com.wideplay.crosstalk.data.Occupancy;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.RoomTextIndex;
import com.wideplay.crosstalk.data.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class RoomStore {

  @Inject
  private Objectify objectify;

  public RoomStore() {
  }

  public Room byId(Long id) {
    Room room = objectify.find(Room.class, id);
    if (room == null) {
      return null;
    }

    loadOccupancy(room);
    return room;
  }

  private void loadOccupancy(Room room) {
    // Grab the occupancy too (they share the same id).
    Occupancy occupancy = objectify.get(Occupancy.class, room.getId());
    room.setOccupancy(occupancy);
  }

  @Inject
  private Provider<ConnectedClients> connectedClientsProvider;
  private ConnectedClients loadConnectedClients() {
    return connectedClientsProvider.get();
  }

  /**
   * This method is very special, since occupancy is such a high-write
   * data structure, we use a writeback cache instead of the normal
   * objectify-provided writethru cached backed on appengine's memcache.
   */
  public void save(Occupancy occupancy) {
    
    // Overwrites the old occupancy!
    objectify.put(occupancy);
  }

  public Room named(String name) {
    Room room = objectify.query(Room.class).filter("name", name).get();
    if (null == room) {
      return null;
    }

    // Load occupancy.
    loadOccupancy(room);
    return room;
  }

  public void remove(Long roomId) {
    // delete! (orphans both occupancy & posts).
    objectify.delete(Room.class, roomId);
  }

  public List<Room> list() {
    List<Room> list = objectify.query(Room.class).order("startTime").list();
    for (Room room : list) {
      // Load the occupancies too.
      loadOccupancy(room);
    }
    return list;
  }


  public void create(Room room) {
    room.setId(UUID.randomUUID().getMostSignificantBits());

    objectify.put(room);
    // Also create an occupancy with the same id.
    Occupancy occupancy = new Occupancy();
    occupancy.setId(room.getId());
    objectify.put(occupancy);
  }

  public RoomTextIndex indexOf(Room room) {
    return objectify.find(RoomTextIndex.class, room.getId());
  }

  // Returns global index.
  public RoomTextIndex indexOf() {
    return objectify.find(RoomTextIndex.class, 1L);
  }

  public void save(RoomTextIndex index) {
    objectify.put(index);
  }

  public Collection<Room> roomsOf(User user) {
    Collection<Key<Room>> roomKeys = loadConnectedClients().getRooms(user);
    List<Room> rooms = Lists.newArrayList();
    for (Key<Room> roomKey : roomKeys) {
      rooms.add(byId(roomKey.getId()));
    }
    return rooms;
  }

  public void connectClient(User user, String token, Room room) {
    ConnectedClients clients = loadConnectedClients();
    clients.add(token, user, room);
  }

  public String channelOf(Key<User> user, Room room) {
    return loadConnectedClients().channelOf(user.getName(), room);
  }

  public void leaveRoom(Key<User> user, Room room) {
    ConnectedClients clients = loadConnectedClients();
    clients.remove(user, room);
  }
}
