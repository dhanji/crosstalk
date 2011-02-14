package com.wideplay.crosstalk.web;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Post;
import com.wideplay.crosstalk.data.Occupancy;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.store.RoomStore;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/async/index")
public class AsyncIndexService {
  @Inject
  private RoomStore roomStore;

  @Post
  Reply<?> renderActivityBubbles(Request request, Gson gson) {
    Long roomId = Long.valueOf(request.param("room"));
    Room room = roomStore.byId(roomId);

    List<Occupancy.TimeSegment> segments = room.getOccupancy().getSegments();
    if (segments.isEmpty()) {
      return Reply.with("");
    }

    Occupancy.TimeSegment segment = segments.get(segments.size() - 1);
    String segmentHtml = RoomPage.renderActivity(room, segment);

    return Reply.with(gson.toJson(ImmutableMap.of(
        "html", segmentHtml,
        "startsOn", segment.getStartsOn()))
    ).type("application/json");
  }
}
