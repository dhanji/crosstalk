package com.wideplay.crosstalk.web.auth;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.CurrentUser;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Encapsulates the twitter-specific Oauth stuff.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@RequestScoped
public class Twitter {
  private static final Logger log = LoggerFactory.getLogger(Twitter.class);
  public static final String CONSUMER_KEY = "BaBzQIsMvsuEF4e3xpmxQ";
  public static final String CONSUMER_SECRET = "NvNM3KUmfSkWhWxqZzEJmREorVKmd54G7C9jDv1zFw";

  private final OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
  private final OAuthProvider provider = new DefaultOAuthProvider(
      "http://twitter.com/oauth/request_token",
      "http://twitter.com/oauth/access_token",
      "http://twitter.com/oauth/authorize");

  @Inject
  private CurrentUser currentUser;

  @Inject
  private Gson gson;

  @Inject
  private UserStore userStore;

  public static class OAuthRedirect {
    private final String url;
    private final String requestToken;
    private final String tokenSecret;

    public OAuthRedirect(String url, String requestToken, String tokenSecret) {
      this.url = url;
      this.requestToken = requestToken;
      this.tokenSecret = tokenSecret;
    }

    public String getUrl() {
      return url;
    }

    public String getRequestToken() {
      return requestToken;
    }

    public String getTokenSecret() {
      return tokenSecret;
    }
  }

  public OAuthRedirect redirectForAuth() {
    try {
      OAuthRedirect redirect = new OAuthRedirect(provider.retrieveRequestToken(consumer, null),
          consumer.getToken(), consumer.getTokenSecret());

      return redirect;
    } catch (OAuthMessageSignerException e) {
      log.error("Oauth failed", e);
    } catch (OAuthNotAuthorizedException e) {
      log.error("Oauth failed", e);
    } catch (OAuthExpectationFailedException e) {
      log.error("Oauth failed", e);
    } catch (OAuthCommunicationException e) {
      log.error("Oauth failed", e);
    }
    return null;
  }

  public void authorize(String token, String verification) {
    try {
      String secret = userStore.claimOAuthToken(token);
      if (null == secret) {
        throw new IllegalStateException("Unknown oauth request token " + token);
      }

      // "Resume" the oauth dance with the appropriate token and temporary secret.
      consumer.setTokenWithSecret(token, secret);
      provider.retrieveAccessToken(consumer, verification);

      // Now that we have the proper access token, set that on the current user.
      // (Will be saved later).
      User user = currentUser.getUser();
      user.setTwitterAccessToken(consumer.getToken());
      user.setTwitterTokenSecret(consumer.getTokenSecret());

    } catch (OAuthMessageSignerException e) {
      log.error("Oauth failed", e);
    } catch (OAuthNotAuthorizedException e) {
      log.error("Oauth failed", e);
    } catch (OAuthExpectationFailedException e) {
      log.error("Oauth failed", e);
    } catch (OAuthCommunicationException e) {
      log.error("Oauth failed", e);
    }
  }

  /**
   * Makes a signed OAuth call to twitter at this URL, authed as the current user.
   */
  public String call(String urlAsString) {
    User user = currentUser.getUser();
    consumer.setTokenWithSecret(user.getTwitterAccessToken(), user.getTwitterTokenSecret());

    // create an HTTP request to a protected resource
    URL url;
    try {
      url = new URL(urlAsString);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // sign the request
      consumer.sign(connection);

      // send the request
      connection.connect();

      if (connection.getResponseCode() == 200) {
        return IOUtils.toString(connection.getInputStream());
      } else {
        log.error("Twitter returned error code {} with message {}", connection.getResponseCode(),
            connection.getResponseMessage());
      }

    } catch (MalformedURLException e) {
      log.error("Could not perform Twitter OAuth request", e);
    } catch (OAuthExpectationFailedException e) {
      log.error("Could not perform Twitter OAuth request", e);
    } catch (OAuthCommunicationException e) {
      log.error("Could not perform Twitter OAuth request", e);
    } catch (OAuthMessageSignerException e) {
      log.error("Could not perform Twitter OAuth request", e);
    } catch (IOException e) {
      log.error("Could not perform Twitter OAuth request", e);
    }

    return null;
  }
}
