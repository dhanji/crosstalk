package com.wideplay.crosstalk.web;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.wideplay.crosstalk.CrosstalkModule;
import com.wideplay.crosstalk.data.Occupancy;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.store.RoomStore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/async/index") @Service
public class AsyncIndexService {
  private final DateFormat messageDateFormat =
      new SimpleDateFormat(CrosstalkModule.SEGMENT_DATE_FORMAT);

  @Inject
  private RoomStore roomStore;

  @Post
  Reply<?> renderActivityBubbles(ClientRequest request, Gson gson) {
    Long roomId = Long.valueOf(request.getRoom());
    Room room = roomStore.byId(roomId);

    List<Occupancy.TimeSegment> segments = room.getOccupancy().getSegments();
    if (segments.isEmpty()) {
      return Reply.with("");
    }

    StringBuilder builder = new StringBuilder();
    for (Occupancy.TimeSegment segment : segments) {
      builder.append("<div class='segment' title='");
      builder.append(segment.getCount());
      builder.append(" posts around ");
      builder.append(messageDateFormat.format(segment.getStartsOn()));
      builder.append("' starts='");
      builder.append(segment.getStartsOn());
      builder.append("'>");
      builder.append(RoomPage.renderActivity(room, segment));
      builder.append("</div>");
    }

    return Reply.with(gson.toJson(ImmutableMap.of(
        "html", builder.toString()
    ))).type("application/json");
  }
}
