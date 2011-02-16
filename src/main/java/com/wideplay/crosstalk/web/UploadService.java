package com.wideplay.crosstalk.web;

import com.google.appengine.api.datastore.Blob;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.wideplay.crosstalk.data.Attachment;
import com.wideplay.crosstalk.data.store.MessageStore;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/upload") @Service
public class UploadService {
  private static final Logger log = LoggerFactory.getLogger(UploadService.class);

  @Inject
  private CurrentUser currentUser;

  @Inject
  private MessageStore messageStore;

  @Post
  Reply<?> receiveFile(HttpServletRequest request) throws IOException, FileUploadException {
    String fileName = request.getParameter("qqfile");
    log.info("Received upload of file named '{}'", fileName);

      // Get the image representation
//    ServletFileUpload upload = new ServletFileUpload();
//    FileItemIterator iter = upload.getItemIterator(request);
//    FileItemStream file = iter.next();
//    InputStream fileStream = file.openStream();

    // Do something with this stream.
    ServletInputStream inputStream = request.getInputStream();
    byte[] data = IOUtils.toByteArray(inputStream);
    IOUtils.closeQuietly(inputStream);

    Attachment attachment = new Attachment();
    attachment.setId(UUID.randomUUID().getMostSignificantBits());
    attachment.setAuthor(currentUser.getUser());
    attachment.setName(fileName);
    attachment.setContent(new Blob(data));
    attachment.setMimeType(determineMimeType(fileName)); // determine from file name =(

    messageStore.save(attachment);

    log.info("Saving attachment as id [{}]", attachment.getId());

    // Send id back to current user as confirmation.
    return Reply.with("{ success: 'true', id: '" + attachment.getId() + "' }");
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
