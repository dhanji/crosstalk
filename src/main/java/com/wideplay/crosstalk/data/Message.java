package com.wideplay.crosstalk.data;

import com.googlecode.objectify.Key;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * A single message on a room board.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity
public class Message {
  @Id
  private Long id;
  @Embedded
  private User author;

  private Key<Room> room; // belongs to.

  private Date postedOn;
  private String text;
  // TODO(dhanji): Add attachments.


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public void setRoom(Room room) {
    this.room = new Key<Room>(Room.class, room.getId());
  }

  public Date getPostedOn() {
    return postedOn;
  }

  public void setPostedOn(Date postedOn) {
    this.postedOn = postedOn;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "Message{" +
        "id=" + id +
        ", author=" + author +
        ", room=" + room +
        ", postedOn=" + postedOn +
        ", text='" + text + '\'' +
        '}';
  }
}
