package com.wideplay.crosstalk.data;

import com.googlecode.objectify.Key;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity
public class Document {
  @Id
  private Long id;
  private String title;
  private String text;
  private Date createdOn;
  private Date modifiedOn;
  private String author;
  private Key<Room> wiki;

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public Key<Room> getWiki() {
    return wiki;
  }

  public void setWiki(Key<Room> wiki) {
    this.wiki = wiki;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public Date getModifiedOn() {
    return modifiedOn;
  }

  public void setModifiedOn(Date modifiedOn) {
    this.modifiedOn = modifiedOn;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
