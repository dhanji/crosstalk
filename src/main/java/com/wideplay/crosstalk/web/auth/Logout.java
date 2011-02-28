package com.wideplay.crosstalk.web.auth;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.RoomStore;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.Broadcaster;
import com.wideplay.crosstalk.web.CurrentUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/logout") @Service @Singleton
public class Logout {
  @Inject
  private Provider<CurrentUser> currentUserProvider;

  @Inject
  private Provider<UserStore> userStore;

  @Inject
  private Provider<RoomStore> roomStore;

  @Inject
  private Broadcaster broadcaster;

  @Get
  Reply<?> logout(HttpServletResponse response, Gson gson) {
    CurrentUser currentUser = currentUserProvider.get();

    // Unset session cookie by resetting its max age.
    Cookie cookie = currentUser.getSessionCookie();
    if (null != cookie) {
      cookie.setMaxAge(0);
      response.addCookie(cookie);

      // Kick user out of all rooms.
      User leaver = currentUser.getUser();
      Collection<Room> rooms = roomStore.get().roomsOf(leaver);
      
      for (Room room : rooms) {
        broadcaster.broadcast(room, leaver, gson.toJson(ImmutableMap.of(
            "rpc", "leave",
            "leaver", leaver
        )));
      }

      // Remove user from session store too.
      userStore.get().logout(cookie.getValue());
    }

    return Reply.saying().redirect("/");
  }
}
