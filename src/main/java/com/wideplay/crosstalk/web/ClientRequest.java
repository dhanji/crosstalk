package com.wideplay.crosstalk.web;

/**
 * Value object to receive JSON data from client.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class ClientRequest {
  private String text;
  private String token;
  private Long room;
  private Long attachmentId;

  public Long getRoom() {
    return room;
  }

  public String getToken() {
    return token;
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return "ClientRequest{" +
        ", text='" + text + '\'' +
        '}';
  }

  public Long getAttachmentId() {
    return attachmentId;
  }
}
