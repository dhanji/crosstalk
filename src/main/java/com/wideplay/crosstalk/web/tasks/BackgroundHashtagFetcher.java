package com.wideplay.crosstalk.web.tasks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Text;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Appengine callback queue.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/queue/hashtag") @Service
public class BackgroundHashtagFetcher {
  private static final Type LIST_TYPE = new TypeToken<List<Map<String, Object>>>() {}.getType();
  private static final Logger log = LoggerFactory.getLogger(BackgroundHashtagFetcher.class);

  @Inject
  Web web;

  @Get
  Reply<?> retrieveHashtags(Gson gson) {
    String hashtag = URLEncoder.encode("#webstock");
    WebResponse response = web.clientOf("http://search.twitter.com/search.json?q=" + hashtag)
        .transports(String.class)
        .over(Text.class)
        .get();

    log.info("Fetched for hashtag {}, all these tweeeeeets! {}", hashtag, response.toString());
    List<Map<String, Object>> tweets = gson.fromJson(response.toString(), LIST_TYPE);

    return Reply.saying().ok();
  }
}
