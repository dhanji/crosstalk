package com.wideplay.crosstalk.data;

import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.Key;

import javax.persistence.Id;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Attachment {
  @Id
  private Long id;
  private String name;
  private String mimeType;

  @JsonHide
  private Key<User> author;
  @JsonHide
  private Blob content;

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

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public Key<User> getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = new Key<User>(User.class, author.getUsername());
  }

  public Blob getContent() {
    return content;
  }

  public void setContent(Blob content) {
    this.content = content;
  }
}
