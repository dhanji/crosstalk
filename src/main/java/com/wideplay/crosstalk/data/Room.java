package com.wideplay.crosstalk.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;

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
  private String host;

  private Date startTime;
  private Date endTime;

  // Loaded independently.
  @Transient
  private Occupancy occupancy;

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
    setDisplayName(name);

    name = name.toLowerCase().replaceAll("[ ]+", "-");
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

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPeriod(Date startTime, Date endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Room)) return false;

    Room room = (Room) o;

    if (id != null ? !id.equals(room.id) : room.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
