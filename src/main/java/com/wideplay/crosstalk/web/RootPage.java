package com.wideplay.crosstalk.web;

import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;

/**
 * This page exists only to redirect to /home for security.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/") @Service
public class RootPage {
  @Get
  Reply<?> redirect() {
    return Reply.saying().redirect("/r/home");
  }
}
