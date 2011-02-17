package com.wideplay.crosstalk.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The occupancy state of a room. Current and unique.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Cached @Entity
public class Occupancy {
  public static final int TIME_SEGMENT_INTERVAL_MINS = 5;
  @Id
  private Long id;

  private Set<Key<User>> users = Sets.newLinkedHashSet();

  @Embedded
  private List<TimeSegment> segments = Lists.newArrayList();

  private Set<String> terms = Sets.newLinkedHashSet();

  @Transient @JsonHide
  private int maxActivity = -1; // memo field.

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void add(User user) {
    users.add(new Key<User>(User.class, user.getUsername()));
  }

  public Set<Key<User>> getUsers() {
    return users;
  }

  public List<TimeSegment> getSegments() {
    return segments;
  }

  public Set<String> getTerms() {
    return terms;
  }

  public int getMaxActivity() {
    if (maxActivity == -1) {
      for (TimeSegment segment : segments) {
        if (segment.count > maxActivity)
          maxActivity = segment.count;
      }
    }

    return maxActivity;
  }

  /**
   * Picks a user at random (tries to be fair).
   */
  public Key<User> pickUser() {
    if (users.isEmpty()) {
      return null;
    }
    List<Key<User>> userKeys = Lists.newArrayList();

    // Eliminate the anonymous user from the selection set.
    for (Key<User> userKey : this.users) {
      if (!User.ANONYMOUS.getUsername().equals(userKey.getName())) {
        userKeys.add(userKey);
      }
    }

    return pickUser(userKeys);
  }

  private Key<User> pickUser(List<Key<User>> userKeys) {
    if (userKeys.isEmpty()) {
      return null;
    }
    return userKeys.get((int) ((Math.random() * userKeys.size()) % userKeys.size()));
  }

  @SuppressWarnings("deprecation") // Calendar is just too awful to use.
  public void incrementNow() {
    // first determine if a new time segment is needed.
    Date now = new Date();
    int slot = now.getMinutes() / TIME_SEGMENT_INTERVAL_MINS;

    if (segments.isEmpty()) {
      // Insert a brand new time segment!
      segments.add(newSegment(now));

    } else {
      TimeSegment timeSegment = segments.get(segments.size() - 1);
      int prevSlot = timeSegment.getStartsOn().getMinutes() / TIME_SEGMENT_INTERVAL_MINS;
      if (slot > prevSlot || slot == 0) {
        // This is a new time segment!
        segments.add(newSegment(now));
      } else {
        // Increment the last segment.
        timeSegment.count++;
      }
    }
  }

  private static TimeSegment newSegment(Date now) {
    TimeSegment newSegment = new TimeSegment();
    newSegment.startsOn = now;
    newSegment.count = 1;
    return newSegment;
  }

  public static class TimeSegment {
    private Date startsOn;
    private int count;

    public Date getStartsOn() {
      return startsOn;
    }

    public int getCount() {
      return count;
    }

    @Override
    public String toString() {
      return startsOn + ":" + count;
    }
  }
}
