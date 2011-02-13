package com.wideplay.crosstalk.data.store;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Room;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStore {
  @Inject
  private Objectify objectify;

  public void save(Message message) {
    objectify.put(message);
  }

  public List<Message> list(Room room) {
    // TODO dont load the entire list, instead paginate.
    return objectify.query(Message.class).filter("room.id", room.getId()).list();
  }

  public void save(Attachment attachment) {
    objectify.put(attachment);
  }

  public Attachment fetch(Long id) {
    return objectify.find(Attachment.class, id);
  }
}
