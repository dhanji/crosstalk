package com.wideplay.crosstalk.data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Encapuslates a structured set of documents. Contains an ACL.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity
public class Room {
  private static final Room DEFAULT;

  static {
    DEFAULT = new Room();
    DEFAULT.setId(1L);
    DEFAULT.setName("Home");
    DEFAULT.setOccupancy(new Occupancy());
  }

  @Id
  private Long id;
  private String name;
  private String displayName;

  private transient Occupancy occupancy;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Occupancy getOccupancy() {
    return occupancy;
  }

  public void setOccupancy(Occupancy occupancy) {
    this.occupancy = occupancy;
  }
}
