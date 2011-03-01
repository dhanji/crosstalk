package com.wideplay.crosstalk.web.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.googlecode.objectify.Key;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.buzz.BuzzSearch;
import com.wideplay.crosstalk.data.store.MessageStore;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.Broadcaster;
import com.wideplay.crosstalk.web.auth.buzz.BuzzApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/queue/buzz")
@Service
public class BackgroundBuzzService {
  private static final Logger log = LoggerFactory.getLogger(BackgroundBuzzService.class);
  private static final int MAX_TERMS = 4;

  @Inject
  private RoomStore roomStore;

  @Inject
  private MessageStore messageStore;

  @Inject
  private UserStore userStore;

  @Inject
  private Broadcaster broadcaster;

  @Get
  Reply<?> pullBuzzes(BuzzApi buzz, Gson gson) {
    // Do this for all rooms.
    List<Room> rooms = roomStore.list();
    log.info("Starting background buzz fetch...");

    // Pick an arbitrary user to be our buzz patsy.
    for (Room room : rooms) {
      Key<User> userKey = room.getOccupancy().pickUser();
      if (null == userKey) {
        // skip room, there's no one here.
        continue;
      }

      User patsy = userStore.fetch(userKey);
      log.info("Patsy found: {}!", patsy.getUsername());

      int termsFetched = 0;
      for (String term : room.getOccupancy().getTerms()) {
        // This can get out of hand, so cap it.
        if (termsFetched > MAX_TERMS) {
          break;
        }

        term = URLEncoder.encode(term);
        String result = buzz.call(patsy, "https://www.googleapis.com/buzz/v1/activities/search?alt=json&q=" + term);
        // Call to twitter can fail for various reasons.
        if (result != null && !result.isEmpty()) {
          BuzzSearch buzzes = gson.fromJson(result, BuzzSearch.Data.class).getData();

          // Select a tweet and broadcast.
          Message pick = buzzes.pick();

          log.info("Found {} results for {}, picking buzz...", buzzes.getItems().size(), term);
          if (null != pick) {
            // Skip sending this tweet if it already exists in the room.
            Message message = messageStore.fetchMessage(pick.getId());
            if (message != null && room.getId().equals(message.getRoomKey().getId())) {
              continue;
            }

            pick.setRoom(room);
            broadcaster.broadcast(room, null, gson.toJson(
                ImmutableMap.of(
                    "rpc", "tweet",
                    "post", pick)
            ));

            // If we liked this tweet, insert it into the room log.
            userStore.createGhost(pick.getAuthor());
            messageStore.save(pick);
          }
        }

        termsFetched++;
      }

      // Use this opportunity to perform room stateness evictions.
      room.getOccupancy().getUsers();
    }

    // Chain next instance of this task.
    TaskQueue.enqueueBuzzTask();

    return Reply.saying().ok();
  }

}
