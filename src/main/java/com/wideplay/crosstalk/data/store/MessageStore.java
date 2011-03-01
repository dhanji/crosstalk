package com.wideplay.crosstalk.data.store;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import com.wideplay.crosstalk.data.Attachment;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStore {
  @Inject
  private Objectify objectify;

  @Inject
  private UserStore userStore;

  public Message fetchMessage(Long id) {
    return objectify.find(Message.class, id);
  }

  public void save(Message message) {
    objectify.put(message);
  }

  public List<Message> list(Room room) {
    // TODO dont load the entire list, instead paginate with cursors.
    List<Message> list = objectify
        .query(Message.class)
        .filter("roomKey", new Key<Room>(Room.class, room.getId()))
        .order("postedOn")
        .list();
    resolveUsers(list);

    return list;
  }

  private void resolveUsers(List<Message> list) {
    Set<Key<User>> userKeys = Sets.newHashSet();
    for (Message message : list) {
      userKeys.add(message.getAuthorKey());
    }

    Map<Key<User>, User> users = userStore.resolve(userKeys);

    // Set these users back on the messages. This seems a bit expensive. =(
    for (Message message : list) {
      message.setAuthor(users.get(message.getAuthorKey()));
    }
  }

  public List<Message> listRecent(int max) {
    Query<Message> results = objectify.query(Message.class)
        .order("-postedOn");

    List<Message> picks = Lists.newArrayList();
    int i = 0;
    for (Message pick : results) {
      picks.add(pick);
      if (i > max) {
        break;
      }
      i++;
    }

    resolveUsers(picks);

    return picks;
  }

  public void save(Attachment attachment) {
    objectify.put(attachment);
  }

  public Attachment fetchAttachment(Long id) {
    return objectify.find(Attachment.class, id);
  }
}
