package com.wideplay.crosstalk.data.store;

import com.wideplay.crosstalk.data.Room;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class RoomStore {
  public Room byId(Long id) {
    return id == 1L ? Room.DEFAULT : null;
  }
}
