package com.wideplay.crosstalk.web;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;

/**
 * Debug class, unused in production app.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/ot")
public class OTDebug {
  @Inject
  private UserService userService;

  @Inject
  private ChannelService channelService;

  @Inject
  AsyncPostService.ConnectedClients connected;

  private String token;

  @Get
  void get() {
    // Create channel token specific to this user.
    String channelKey = "" + Math.random();
    token = channelService.createChannel(channelKey);

    connected.add(token, channelKey);
  }

  public String getToken() {
    return token;
  }

}
