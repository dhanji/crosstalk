package com.wideplay.crosstalk.web;

import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.store.RoomStore;

import java.util.List;

/**
 * TODO secure!
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/room_admin")
public class RoomAdminPage {
  @Inject
  private RoomStore roomStore;

  private List<Room> rooms;

  @Get
  void displayRooms() {
    rooms = roomStore.list();
  }

  @Post
  void newRoom(Request request) {
    String name = request.param("name");
    roomStore.create(name);
  }

  @Delete
  void deleteRoom(Request request) {
    Long roomId = Long.valueOf(request.param("room-id"));
    roomStore.remove(roomId);
  }

  public List<Room> getRooms() {
    return rooms;
  }
}
