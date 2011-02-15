package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.At;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.googlecode.objectify.Key;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.MessageStore;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.data.twitter.TwitterSearch;
import com.wideplay.crosstalk.web.auth.Twitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
* @author dhanji@gmail.com (Dhanji R. Prasanna)
*/
@At("/r/async") @Service
public class AsyncPostService {
  private static final Logger log = LoggerFactory.getLogger(AsyncPostService.class);

  @Inject
  private ConnectedClients connected;

  @Inject
  private ChannelService channel;

  @Inject
  private CurrentUser currentUser;

  @Inject
  private RoomStore roomStore;

  @Inject
  private MessageStore messageStore;

  @Inject
  private Gson gson;

  @Inject
  private Web web;


  @Singleton
  public static class ConnectedClients {
    // TODO(dhanji): This needs to be persistent. And needs periodic eviction.
    private final Map<String, Map<Room, String>> clients = new MapMaker()
        .makeComputingMap(new Function<String, Map<Room, String>>() {
          @Override
          public Map<Room, String> apply(String user) {
            return new MapMaker().makeMap();
          }
        });

    public void add(String token, User client, Room room) {
      clients.get(client.getUsername()).put(room, token);
    }
  }

  @At("/message") @Post
  Reply<?> receiveMessage(ClientRequest request) {
    Room room = roomStore.byId(request.getRoom());

    Message message = new Message();
    message.setId(UUID.randomUUID().getMostSignificantBits());
    message.setText(request.getText());
    message.setRoom(room);
    message.setPostedOn(new Date());

    // Temporary hack while we dont have proper user db.
    User author = currentUser.getUser();
    message.setAuthor(author);

    log.info("Received post {}", message);

    // Reflect to other clients.
    String json = gson.toJson(ImmutableMap.of(
        "rpc", "receive",
        "post", message
    ));
    broadcast(room, author, json);

    // Save AFTER broadcast (reduces latency).
    room.getOccupancy().incrementNow(); // Increment activity in the room by 1.
    roomStore.save(room.getOccupancy());
    messageStore.save(message);

    return Reply.saying().ok();
  }

  @At("/join") @Post
  Reply<?> joinRoom(ClientRequest request) {
    Room room = roomStore.byId(request.getRoom());

    User joiner = currentUser.getUser();
    log.debug("Received join notification: {}", joiner.getUsername());
    String json = gson.toJson(ImmutableMap.of(
        "rpc", "join",
        "joiner", joiner
    ));
    broadcast(room, joiner, json);

    // This is a bit hacky, but we update occupancy BEFORE this RPC is called
    // in the home screen. We should move it here.

    return Reply.saying().ok();
  }

  @At("/leave") @Post
  Reply<?> leaveRoom(ClientRequest request) {
    Room room = roomStore.byId(request.getRoom());

    User leaver = currentUser.getUser();
    log.info("Received leave notification: {}", leaver.getUsername());
    String json = gson.toJson(ImmutableMap.of(
        "rpc", "leave",
        "leaver", leaver
    ));
    broadcast(room, leaver, json);

    // Update occupancy.
    room.getOccupancy().getUsers().remove(new Key<User>(User.class, leaver.getUsername()));
    roomStore.save(room.getOccupancy());

    return Reply.saying().ok();
  }

  @At("/ping") @Post
  Reply<?> ping(ClientRequest request, Twitter twitter, CurrentUser currentUser) {
    String hashtag = URLEncoder.encode("#webstock");

    // Update active status timestamp of this user/connection
    //...

    // Only piggyback the twitter call if this user is logged in.
    if (!currentUser.isAnonymous()) {
      String result = twitter.call("http://search.twitter.com/search.json?q=" + hashtag);
      TwitterSearch tweets = gson.fromJson(result, TwitterSearch.class);

      // Select a tweet and broadcast.
      Message pick = tweets.pick();
      if (null != pick) {
        broadcast(roomStore.byId(Long.valueOf(request.getRoom())), null, gson.toJson(
            ImmutableMap.of(
                "rpc", "tweet",
                "post", pick)
        ));
      }
    }

    return Reply.saying().ok();
  }

  @At("/add-term") @Post
  Reply<?> addTerm(ClientRequest request) {
    log.info("New term added {}", request.getText());

    return Reply.saying().ok();
  }

  @At("/remove-term") @Post
  Reply<?> removeTerm(ClientRequest request) {
    log.info("Term deleted {}", request.getText());

    return Reply.saying().ok();
  }

  private void broadcast(Room room, User author, String json) {
    for (Key<User> user : room.getOccupancy().getUsers()) {
      if (null != author && user.getName().equals(author.getUsername()))
        continue;

      log.info("Sending packet to {} [{}]\n", user.getName(), json);
      String channelId = connected.clients.get(user.getName()).get(room);
      if (null != channelId) {
        channel.sendMessage(new ChannelMessage(channelId, json));
      } else {
        // stale occupancy, update...

      }
    }
  }
}
