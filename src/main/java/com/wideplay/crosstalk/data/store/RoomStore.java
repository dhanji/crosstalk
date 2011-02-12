package com.wideplay.crosstalk.data.store;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.google.inject.Singleton;
import com.wideplay.crosstalk.data.Room;

import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class RoomStore {
  private final Map<Long, Room> rooms = new MapMaker().makeMap();

  public RoomStore() {
    rooms.put(1L, Room.DEFAULT);
  }

  public Room byId(Long id) {
    return rooms.get(id);
  }

  public void remove(Long roomId) {
    // delete!
    rooms.remove(roomId);
  }

  public List<Room> list() {
    return ImmutableList.copyOf(rooms.values());
  }

  public void create(String name) {
    Room room = new Room();
    room.setId((long)Math.random());
    room.setName(name);
    rooms.put(room.getId(), room);
  }
}
