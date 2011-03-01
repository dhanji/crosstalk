package com.wideplay.crosstalk.data.indexing;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * A simple utility for identifying a fixed set of stopwords.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class StopWords {
  private final Set<String> words;

  public StopWords() throws IOException {
    @SuppressWarnings("unchecked")
    List<String> lines = IOUtils.readLines(StopWords.class.getResourceAsStream("stopwords.txt"));

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (String line : lines) {
      if (line.isEmpty()) {
        continue;
      }
      builder.add(line);
    }
    this.words = builder.build();
  }

  public boolean isStopWord(String word) {
    // Anything below 3 characters is automatically a stop word.
    if (word.length() < 4 && word.length() < 14) {
      return true;
    }

    return words.contains(word);
  }
}