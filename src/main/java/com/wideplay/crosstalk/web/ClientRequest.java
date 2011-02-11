package com.wideplay.crosstalk.web;

/**
 * Value object to receive JSON data from client.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class ClientRequest {
  private String text;
  private String token;

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
}
