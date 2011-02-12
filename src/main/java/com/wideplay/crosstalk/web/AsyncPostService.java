package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.RoomStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  ConnectedClients connected;

  @Inject
  ChannelService channel;

  @Inject
  private CurrentUser currentUser;

  @Inject
  private RoomStore roomStore;

  @Inject
  Gson gson;

  @Singleton
  public static class ConnectedClients {
    private final Map<User, String> clients = Maps.newHashMap();

    public void add(String token, User client) {
      clients.put(client, token);
    }
  }

  @At("/message") @Post
  Reply<?> receiveMessage(ClientRequest request) {
    Message message = new Message();
    message.setId(UUID.randomUUID().getMostSignificantBits());
    message.setText(request.getText());
    message.setRoom(roomStore.byId(request.getRoom()));
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
    broadcast(author, json);

    return Reply.saying().ok();
  }

  private void broadcast(User author, String json) {
    for (User user : Room.DEFAULT.getOccupancy().getUsers()) {
      if (user.equals(author))
        continue;

      log.info("Sending packet to {} [{}]\n", user.getUsername(), json);
      channel.sendMessage(new ChannelMessage(user.getUsername(), json));
    }
  }

  @At("/join") @Post
  Reply<?> joinRoom() {
    User joiner = currentUser.getUser();
    log.info("Received join notification: {}", joiner.getUsername());
    String json = gson.toJson(ImmutableMap.of(
        "rpc", "join",
        "joiner", joiner
    ));
    broadcast(joiner, json);
    return Reply.saying().ok();
  }
}
