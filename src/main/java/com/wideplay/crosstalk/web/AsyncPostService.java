package com.wideplay.crosstalk.web;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.googlecode.objectify.Key;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Occupancy;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.MessageStore;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.web.auth.Secure;
import com.wideplay.crosstalk.web.auth.Twitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Date;
import java.util.UUID;

/**
* @author dhanji@gmail.com (Dhanji R. Prasanna)
*/
@At("/r/async") @Service
public class AsyncPostService {
  private static final Logger log = LoggerFactory.getLogger(AsyncPostService.class);

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

  @Inject
  private Broadcaster broadcaster;

  @At("/message") @Post @Secure
  Reply<?> receiveMessage(ClientRequest request) {
    Room room = roomStore.byId(request.getRoom());

    Message message = new Message();
    message.setId(UUID.randomUUID().getMostSignificantBits());
    message.setText(request.getText());
    message.setRoom(room);
    message.setPostedOn(new Date());
    if (request.getAttachmentId() != null) {
      message.setAttachment(request.getAttachmentId());
    }

    // Temporary hack while we dont have proper user db.
    User author = currentUser.getUser();
    message.setAuthor(author);

    log.info("Received post {}", message);

    // Reflect to other clients.
    String json = gson.toJson(ImmutableMap.of(
        "rpc", "receive",
        "post", message
    ));
    broadcaster.broadcast(room, author, json);

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
    broadcaster.broadcast(room, joiner, json);

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
    broadcaster.broadcast(room, leaver, json);

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

    return Reply.saying().ok();
  }

  @At("/add-term") @Post @Secure
  Reply<?> addTerm(ClientRequest request) {

    Room room = roomStore.byId(request.getRoom());
    Occupancy occupancy = room.getOccupancy();
    String term = request.getText();
    if (!occupancy.getTerms().contains(term)) {
      occupancy.getTerms().add(term);
      roomStore.save(occupancy);
      log.info("New term added {} in room {}", term, room.getName());
    }

    return Reply.saying().ok();
  }

  @At("/remove-term") @Post @Secure
  Reply<?> removeTerm(ClientRequest request) {

    Room room = roomStore.byId(request.getRoom());
    Occupancy occupancy = room.getOccupancy();
    String term = request.getText();
    if (occupancy.getTerms().contains(term)) {
      occupancy.getTerms().remove(term);
      roomStore.save(occupancy);
      log.info("Term deleted {} in room {}", term, room.getName());
    }

    return Reply.saying().ok();
  }
}
