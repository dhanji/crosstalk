package com.wideplay.crosstalk;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.sitebricks.headless.Request;
import com.wideplay.crosstalk.data.store.StoreModule;
import com.wideplay.crosstalk.web.ClientRequest;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class CrosstalkModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new StoreModule());
  }

  @Provides
  @Singleton
  Gson provideGson() {
    return new GsonBuilder().setDateFormat("HH:mm a").create();
  }

  @Provides
  ChannelService provideChannelService() {
    return ChannelServiceFactory.getChannelService();
  }

  @Provides @RequestScoped
  ClientRequest provideClientRequest(Request request, Gson gson) {
    return gson.fromJson(request.param("data"), ClientRequest.class);
  }
}
