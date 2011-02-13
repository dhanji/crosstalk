package com.wideplay.crosstalk.web;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Raw;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.wideplay.crosstalk.data.store.Attachment;
import com.wideplay.crosstalk.data.store.MessageStore;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Serves attachments. Complement to Upload.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/download/:id") @Service
public class Download {
  private static final Logger log = LoggerFactory.getLogger(Download.class);

  @Inject
  private CurrentUser currentUser;

  @Inject
  private MessageStore messageStore;

  @Post
  Reply<?> sendFile(@Named("id") String id) throws IOException, FileUploadException {
    Long attachmentId = Long.valueOf(id);

    Attachment attachment = messageStore.fetch(attachmentId);
    return Reply
        .with(attachment.getContent().getBytes())
        .type(attachment.getMimeType())
        .as(Raw.class);
  }

  private String determineMimeType(String fileName) {
    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (fileName.endsWith(".gif")) {
      return "image/gif";
    } else if (fileName.endsWith(".png")) {
      return "image/png";
    }
    // Unknown.
    return "application/octet-stream";
  }
}
