package com.wideplay.crosstalk;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.headless.Request;
import com.wideplay.crosstalk.data.JsonHide;
import com.wideplay.crosstalk.data.store.StoreModule;
import com.wideplay.crosstalk.web.ClientRequest;
import com.wideplay.crosstalk.web.SitebricksConfig;
import com.wideplay.crosstalk.web.WebModule;
import com.wideplay.crosstalk.web.tasks.BackgroundTasksModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class CrosstalkModule extends SitebricksModule {
  public static final String POST_DATE_FORMAT = "HH:mm";
  public static final String SEGMENT_DATE_FORMAT = "HH:m0";

  @Override
  protected void configureSitebricks() {
    scan(SitebricksConfig.class.getPackage());

    install(new BackgroundTasksModule());
    install(new WebModule());
    install(new StoreModule());
  }

  private static final ExclusionStrategy EXCLUDE = new ExclusionStrategy() {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
      return fieldAttributes.getAnnotation(JsonHide.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
      return false;
    }
  };

  @Provides
  @Singleton
  Gson provideGson() {
    return new GsonBuilder().setDateFormat(POST_DATE_FORMAT)
        .setExclusionStrategies(EXCLUDE)
        .create();
  }

  @Provides
  ChannelService provideChannelService() {
    return ChannelServiceFactory.getChannelService();
  }

  @Provides
  @RequestScoped
  ClientRequest provideClientRequest(Request request, Gson gson) {
    return gson.fromJson(request.param("data"), ClientRequest.class);
  }
}
