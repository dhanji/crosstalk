package com.wideplay.crosstalk.web.tasks;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.PorterStemmer;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.StopWords;
import com.wideplay.crosstalk.data.store.MessageStore;
import com.wideplay.crosstalk.data.store.RoomStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Runs in the background and generates text clusters
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/queue/cluster") @Service 
public class BackgroundTextClusterer {
  private static final Logger log = LoggerFactory.getLogger(BackgroundTextClusterer.class);

  @Inject
  private StopWords stopWords;

  @Inject
  private RoomStore roomStore;

  @Inject
  private MessageStore messageStore;

  @Get
  Reply<?> clusterPosts() {
    log.info("Start background clustering...");

    try {
    List<Room> rooms = roomStore.list();
    for (Room room : rooms) {
      List<Message> messages = messageStore.list(room);

      Map<String, Integer> wordCount = Maps.newHashMap();
      for (Message message : messages) {
        String text = message.getText();
        String[] words = text.split("[ ,.-;:'\"()!?+*&]+");
        for (String word : words) {
          word = word.toLowerCase();

          // Stem word to its room form.
          word = PorterStemmer.stem(word); // should we bother?
          
          if (!stopWords.isStopWord(word)) {
            Integer count = wordCount.get(word);
            if (null == count) {
              count = 0;
            }

            wordCount.put(word, count + 1);
          }
        }
      }

      log.info("Trending topics for the current room {}", wordCount);
    }
    } catch (Exception e) {
      log.error("Error in clustering...", e);
    }
    return Reply.saying().ok();
  }
}
