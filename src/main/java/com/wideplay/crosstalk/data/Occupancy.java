package com.wideplay.crosstalk.data;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * The occupancy state of a room. Current and unique.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Occupancy {
  private Set<User> users = Sets.newLinkedHashSet();

  public Set<User> getUsers() {
    return users;
  }
}
