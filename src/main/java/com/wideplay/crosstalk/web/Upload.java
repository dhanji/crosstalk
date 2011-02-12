package com.wideplay.crosstalk.web;

import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/r/upload") @Service
public class Upload {
  private static final Logger log = LoggerFactory.getLogger(Upload.class);

  @Post
  Reply<?> receiveFile(HttpServletRequest request) throws IOException, FileUploadException {
    log.info("Received upload of file named '{}'", request.getParameter("qqfile"));

      // Get the image representation
    ServletFileUpload upload = new ServletFileUpload();
    FileItemIterator iter = upload.getItemIterator(request);
    FileItemStream file = iter.next();
    InputStream fileStream = file.openStream();

    // Do something with this stream.
    IOUtils.toByteArray(fileStream);
//    Blob imageBlob = new Blob(IOUtils.toByteArray(imgStream));

    IOUtils.closeQuietly(fileStream);
    return Reply.with("{ success: 'true' }");
  }
}
