package com.wideplay.crosstalk.web.tasks;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.Message;
import com.wideplay.crosstalk.data.Room;
import com.wideplay.crosstalk.data.RoomTextIndex;
import com.wideplay.crosstalk.data.indexing.StopWords;
import com.wideplay.crosstalk.data.store.MessageStore;
import com.wideplay.crosstalk.data.store.RoomStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Runs in the background and generates text clusters with simple counting.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/queue/cluster")
@Service
public class BackgroundTextClusterer {
  private static final Logger log = LoggerFactory.getLogger(BackgroundTextClusterer.class);
  public static final int MAX_WORDS = 150;

  @Inject
  private StopWords stopWords;

  @Inject
  private RoomStore roomStore;

  @Inject
  private MessageStore messageStore;

  @Get
  Reply<?> clusterPosts() {
    log.info("Starting background clustering...");

    // Gather info about all rooms.
    RoomTextIndex globalIndex = new RoomTextIndex();
    Map<String, Integer> globalWordCount = Maps.newHashMap();
    globalIndex.setId(1L);

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
//          word = PorterStemmer.stem(word); // should we bother?

          if (!stopWords.isStopWord(word)) {
            Integer count = wordCount.get(word);
            Integer globalCount = globalWordCount.get(word);
            if (null == count) {
              count = 0;
              globalCount = 0;
            }

            wordCount.put(word, count + 1);
            globalWordCount.put(word, globalCount + 1);
          }
        }
      }

      List<RoomTextIndex.WordTuple> words = toWordList(wordCount);

      // O(N log N)
      Collections.sort(words);

      // Only keep 50 words around in our index.
      if (!words.isEmpty()) {
        words = words.subList(0, Math.min(words.size(), MAX_WORDS));
      }

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

    // Save global word count.
    List<RoomTextIndex.WordTuple> globalWords = toWordList(globalWordCount);
    Collections.sort(globalWords);
    globalIndex.setWords(globalWords);
    roomStore.save(globalIndex);

    // Chain next instance of this task.
    TaskQueue.enqueueClusterTask();

    return Reply.saying().ok();
  }

  private static List<RoomTextIndex.WordTuple> toWordList(Map<String, Integer> wordCount) {
    List<RoomTextIndex.WordTuple> words = Lists.newArrayListWithExpectedSize(wordCount.size());
    for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
      RoomTextIndex.WordTuple wordTuple = new RoomTextIndex.WordTuple();
      wordTuple.set(entry.getKey(), entry.getValue());
      words.add(wordTuple);
    }
    return words;
  }

  private static Multimap<String, Room> multimap() {
    return Multimaps.newListMultimap(Maps.<String, Collection<Room>>newHashMap(),
        new Supplier<List<Room>>() {
      public List<Room> get() {
        return Lists.newArrayList();
      }
    });
  }
}
