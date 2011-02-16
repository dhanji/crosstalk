package com.wideplay.crosstalk.web;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.wideplay.crosstalk.data.Attachment;
import com.wideplay.crosstalk.data.store.MessageStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves attachments. Complement to Upload.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/attachment/:id") @Service
public class AttachmentService {
  private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

  @Inject
  private CurrentUser currentUser;

  @Inject
  private MessageStore messageStore;

  @Get
  Reply<?> sendFile(@Named("id") String id, HttpServletResponse response) throws IOException {
    Long attachmentId = Long.valueOf(id);

    Attachment attachment = messageStore.fetchAttachment(attachmentId);
    if (null == attachment) {
      log.warn("No attachment found for id {}", attachmentId);
      return Reply.saying().notFound();
    }

    byte[] bytes = attachment.getContent().getBytes();
    response.setContentType(attachment.getMimeType());
    response.setContentLength(bytes.length);
    ServletOutputStream out = response.getOutputStream();
    IOUtils.write(bytes, out);
    IOUtils.closeQuietly(out);

    return Reply.saying().ok();
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
