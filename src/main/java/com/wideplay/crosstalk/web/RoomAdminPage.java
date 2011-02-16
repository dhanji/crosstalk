package com.wideplay.crosstalk.web;

import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.web.auth.AdminOnly;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

  @Get @AdminOnly
  void displayRooms() {
    rooms = roomStore.list();
  }

  @Post @AdminOnly
  String newRoom(Request request) throws ParseException {
    Room room = new Room();
    room.setName(request.param("name"));
    room.setHost(request.param("host"));

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    Date startTime = dateFormat.parse(request.param("startTime"));
    Date endTime = dateFormat.parse(request.param("endTime"));
    room.setPeriod(startTime, endTime);

    roomStore.create(room);

    // redirect back here!
    return "/r/room_admin";
  }

  @Delete @AdminOnly
  void deleteRoom(Request request) {
    Long roomId = Long.valueOf(request.param("id"));
    roomStore.remove(roomId);
  }

  public List<Room> getRooms() {
    return rooms;
  }
}
