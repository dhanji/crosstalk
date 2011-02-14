package com.wideplay.crosstalk.data;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.google.inject.Singleton;

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
    List<String> lines = Resources.readLines(StopWords.class.getResource("stopwords.txt"),
        Charsets.UTF_8);

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
    if (word.length() < 4) {
      return true;
    }

    return words.contains(word);
  }
}