package com.wideplay.crosstalk.data;

import com.google.common.collect.Sets;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Set;

/**
 * The occupancy state of a room. Current and unique.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity
public class Occupancy {
  @Id
  private Long id;
  

  @Embedded
  private Set<User> users = Sets.newLinkedHashSet();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Set<User> getUsers() {
    return users;
  }
}
