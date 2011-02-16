package com.wideplay.crosstalk.web.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.*;
import com.wideplay.crosstalk.data.indexing.PorterStemmer;
import com.wideplay.crosstalk.data.RoomTextIndex;
import com.wideplay.crosstalk.data.indexing.StopWords;
import com.wideplay.crosstalk.data.store.MessageStore;
import com.wideplay.crosstalk.data.store.RoomStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Runs in the background and generates text clusters
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/queue/cluster")
@Service
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

      List<RoomTextIndex.WordTuple> words = Lists.newArrayList();
      for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
        RoomTextIndex.WordTuple wordTuple = new RoomTextIndex.WordTuple();
        wordTuple.set(entry.getKey(), entry.getValue());
        words.add(wordTuple);
      }

      // O(N log N)
      Collections.sort(words);

      // TODO(dhanji): Should probably drop all but the top 50 words?
      log.info("Trending topics for the current room {}", words);

      // Update in datastore. We do the if null dance here, coz we
      // need to save this entity anyway, so no point in creating if absent.
      RoomTextIndex index = roomStore.indexOf(room);
      if (null == index) {
        index = new RoomTextIndex();
        index.setRoom(room);
      }
      index.setWords(words);
      roomStore.save(index);
    }

    // Chain next instance of this task.
    TaskQueue.enqueueClusterTask();

    return Reply.saying().ok();
  }
}
