package com.wideplay.crosstalk.web.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.googlecode.objectify.Key;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.MessageStore;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.data.twitter.TwitterSearch;
import com.wideplay.crosstalk.web.Broadcaster;
import com.wideplay.crosstalk.web.auth.Twitter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/queue/tweets")
@Service
public class BackgroundTweetService {
  private static final int MAX_TERMS = 4;

  @Inject
  private RoomStore roomStore;

  @Inject
  private MessageStore messageStore;

  @Inject
  private UserStore userStore;

  @Inject
  private Broadcaster broadcaster;

  @Post
  Reply<?> pullTweets(HttpServletRequest request, Twitter twitter, Gson gson) {
    // Do this for all rooms.
    List<Room> rooms = roomStore.list();

    // Pick an arbitrary user to be our twitter patsy.
    for (Room room : rooms) {
      Key<User> userKey = room.getOccupancy().pickUser();
      if (null == userKey) {
        // skip room, there's no one here.
        continue;
      }

      User patsy = userStore.fetch(userKey);

      // Ghost users can't be used as patsies =(
      if (patsy.isGhost()) {
        continue;
      }

      int termsFetched = 0;
      for (String term : room.getOccupancy().getTerms()) {
        // This can get out of hand, so cap it.
        if (termsFetched > MAX_TERMS) {
          break;
        }
        String result = twitter.call(patsy, "http://search.twitter.com/search.json?q=" + term);
        // Call to twitter can fail for various reasons.
        if (result != null && !result.isEmpty()) {
          TwitterSearch tweets = gson.fromJson(result, TwitterSearch.class);

          // Select a tweet and broadcast.
          Message pick = tweets.pick();
          if (null != pick) {
            pick.setRoom(room);
            broadcaster.broadcast(room, null, gson.toJson(
                ImmutableMap.of(
                    "rpc", "tweet",
                    "post", pick)
            ));

            // If we liked this tweet, insert it into the room log.
            messageStore.save(pick);
          }
        }

        termsFetched++;
      }
    }

    // Chain next instance of this task.
    TaskQueue.enqueueTweetTask();

    return Reply.saying().ok();
  }

}
