package com.wideplay.crosstalk.web;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.Show;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.RoomTextIndex;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.data.store.UserStore;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/") @Show("HomePage.xml")
public class HomePage {
  @Inject
  private RoomStore roomStore;

  @Inject
  private UserStore userStore;

  private List<Room> rooms;

  @Get
  void displayHome() {
    rooms = roomStore.list();
  }

  public List<Room> getRooms() {
    return rooms;
  }

  public Collection<User> occupants(Room room) {
    return userStore.resolve(room.getOccupancy().getUsers()).values();
  }

  public List<RoomTextIndex.WordTuple> trends(Room room) {
    RoomTextIndex index = roomStore.indexOf(room);
    if (null == index) {
      return ImmutableList.of();
    }

    // Only pick the top 4 words.
    return index.getWords().subList(0, Math.min(4, index.getWords().size()));
  }

  public String period(Room room) {
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
    SimpleDateFormat dayFormat = new SimpleDateFormat(", MMM dd");

    return new StringBuilder().append(timeFormat.format(room.getStartTime()))
        .append("-")
        .append(timeFormat.format(room.getEndTime()))
        .append(dayFormat.format(room.getStartTime()))
        .toString();
  }
}
