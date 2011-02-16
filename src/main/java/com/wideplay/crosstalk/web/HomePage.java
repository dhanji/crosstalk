package com.wideplay.crosstalk.web;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.Show;
import com.google.sitebricks.http.Get;
import com.googlecode.objectify.Key;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.RoomTextIndex;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.data.store.UserStore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

  private DateFormat timeFormat = new SimpleDateFormat("hh:mm");
  private DateFormat dayFormat = new SimpleDateFormat(", MMM dd");

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

  public int contributors(Room room) {
    Set<Key<User>> users = room.getOccupancy().getUsers();
    if (users.contains(User.ANONYMOUS_KEY)) {
      return users.size() - 1;
    }

    return users.size();
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

    return new StringBuilder().append(timeFormat.format(room.getStartTime()))
        .append("-")
        .append(timeFormat.format(room.getEndTime()))
        .append(dayFormat.format(room.getStartTime()))
        .toString();
  }

  /**
   * Returns a css class representing the active status of this
   * room based on its session time.
   */
  @SuppressWarnings("deprecation")
  public String status(Room room) {
    Date now = new Date();

    Date startTime = room.getStartTime();
    startTime.setMinutes(startTime.getMinutes() - 15);

    Date endTime = room.getEndTime();
    endTime.setMinutes(endTime.getMinutes() + 15);
    if (now.after(startTime) && now.before(endTime)) {
      return "active";
    } else if (now.after(endTime)) {
      return "future";
    }

    return "inactive";
  }
}
