package com.wideplay.crosstalk.web.auth.buzz;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetAccessToken;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetTemporaryToken;
import com.google.api.client.http.HttpTransport;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.wideplay.crosstalk.data.LoginToken;
import com.wideplay.crosstalk.data.User;
import com.wideplay.crosstalk.data.store.UserStore;
import com.wideplay.crosstalk.web.CurrentUser;
import com.wideplay.crosstalk.web.auth.twitter.Twitter;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Encapsulates the twitter-specific Oauth stuff.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@RequestScoped
public class BuzzApi {
  private static final Logger log = LoggerFactory.getLogger(BuzzApi.class);
  public static final String CONSUMER_KEY = "crosstalkchat.appspot.com";
  public static final String CONSUMER_SECRET = "Z3U9dZKymbTXqAaO6dwM3sV3";
//  public static final String CONSUMER_KEY = "anonymous";
//  public static final String CONSUMER_SECRET = "anonymous";

  @SuppressWarnings("deprecation")
  public static final String BUZZ_SCOPE_READONLY =
      URLEncoder.encode("https://www.googleapis.com/auth/buzz");

  private final OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
  private final OAuthProvider provider = new DefaultOAuthProvider(
      "https://www.google.com/accounts/OAuthGetRequestToken?scope=" + BUZZ_SCOPE_READONLY
      + "&next=" + URLEncoder.encode("http://crosstalk.appspot.com/oauth/buzz"),
      "https://www.google.com/accounts/OAuthGetAccessToken",
      "https://www.google.com/buzz/api/auth/OAuthAuthorizeToken?scope="
          + BUZZ_SCOPE_READONLY
          + "&domain=" + CONSUMER_KEY);

  @Inject
  private CurrentUser currentUser;

  @Inject
  private Gson gson;

  @Inject
  private UserStore userStore;

  private static OAuthHmacSigner signer = new OAuthHmacSigner();

  public Twitter.OAuthRedirect redirectForAuth() {
    try {
      GoogleOAuthGetTemporaryToken temporaryToken = new GoogleOAuthGetTemporaryToken();
//      temporaryToken.transport = Util.AUTH_TRANSPORT;
      signer.clientSharedSecret = CONSUMER_SECRET;
      temporaryToken.signer = signer;
      temporaryToken.consumerKey = CONSUMER_KEY;
      temporaryToken.scope = "https://www.googleapis.com/auth/buzz";
      temporaryToken.displayName = "Crosstalk";
      temporaryToken.callback = "http://crosstalkchat.appspot.com/oauth/buzz";
      OAuthCredentialsResponse tempCredentials = temporaryToken.execute();
      signer.tokenSharedSecret = tempCredentials.tokenSecret;

      // authorization URL
      OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(
          "https://www.google.com/buzz/api/auth/OAuthAuthorizeToken");
      authorizeUrl.set("scope", temporaryToken.scope);
      authorizeUrl.set("domain", CONSUMER_KEY);
      authorizeUrl.set("xoauth_displayname", "Crosstalk");
      authorizeUrl.temporaryToken = tempCredentials.token;
      String url = authorizeUrl.build();

      System.out.println("REQUEST TOKEN REDIRECT URL: " + url);
      return new Twitter.OAuthRedirect(url,
          tempCredentials.token, tempCredentials.tokenSecret);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

//  public Twitter.OAuthRedirect redirectForAuth() {
//    try {
////      Buzz buzz = new Buzz();
////      String url = buzz.getAuthenticationUrl(Buzz.BUZZ_SCOPE_READONLY, CONSUMER_KEY, CONSUMER_SECRET,
////          "http://crosstalk.appspot.com/oauth/buzz");
//          // HACK as last parameter in query string is oauth_callback.
//      provider.setOAuth10a(true);
//      String url = provider.retrieveRequestToken(consumer, null);
//      System.out.println("REQUEST TOKEN REDIRECT URL: " + url);
//      return new Twitter.OAuthRedirect(url,
//          consumer.getToken(), consumer.getTokenSecret());
//    } catch (OAuthMessageSignerException e) {
//      log.error("Oauth failed", e);
//    } catch (OAuthNotAuthorizedException e) {
//      log.error("Oauth failed", e);
//    } catch (OAuthExpectationFailedException e) {
//      log.error("Oauth failed", e);
//    } catch (OAuthCommunicationException e) {
//      log.error("Oauth failed", e);
//    }
////    catch (BuzzAuthenticationException e) {
////      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
////    }
//    return null;
//  }

  public LoginToken authorize(String token, String verification) {
    try {
      LoginToken loginToken = userStore.claimOAuthToken(token);
      // access token

      OAuthCredentialsResponse credentials;
      GoogleOAuthGetAccessToken accessToken = new GoogleOAuthGetAccessToken();
  //    accessToken.transport = Util.AUTH_TRANSPORT;
      accessToken.temporaryToken = token;
      accessToken.signer = signer;
      accessToken.consumerKey = CONSUMER_KEY;
      accessToken.verifier = verification;
      credentials = accessToken.execute();
      signer.tokenSharedSecret = credentials.tokenSecret;

      OAuthParameters authorizer = new OAuthParameters();
      authorizer.consumerKey = CONSUMER_KEY;
      authorizer.signer = signer;
      authorizer.token = credentials.token;
      authorizer.signRequestsUsingAuthorizationHeader(newTransport());

      String secret = loginToken.getTokenSecret();
      if (null == secret) {
        throw new IllegalStateException("Unknown oauth request token " + token);
      }

      // Now that we have the proper access token, set that on the current user.
      // (Will be saved later).
      User user = currentUser.getUser();
      user.setTwitterAccessToken(credentials.token);
      user.setTwitterTokenSecret(credentials.tokenSecret);

      return loginToken;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  static HttpTransport newTransport() {
    HttpTransport result = new HttpTransport();
//    GoogleUtils.useMethodOverride(result);
    GoogleHeaders headers = new GoogleHeaders();
    headers.setApplicationName("Google-BuzzSample/1.0");
    result.defaultHeaders = headers;
    return result;
  }
//  public LoginToken authorize(String token, String verification) {
//    try {
//      LoginToken loginToken = userStore.claimOAuthToken(token);
//      String secret = loginToken.getTokenSecret();
//      if (null == secret) {
//        throw new IllegalStateException("Unknown oauth request token " + token);
//      }
//
//      // "Resume" the oauth dance with the appropriate token and temporary secret.
//      consumer.setTokenWithSecret(token, secret);
//      provider.retrieveAccessToken(consumer, verification);
//
//      // Now that we have the proper access token, set that on the current user.
//      // (Will be saved later).
//      User user = currentUser.getUser();
//      user.setTwitterAccessToken(consumer.getToken());
//      user.setTwitterTokenSecret(consumer.getTokenSecret());
//
//      return loginToken;
//    } catch (OAuthMessageSignerException e) {
//      log.error("Oauth failed", e);
//    } catch (OAuthNotAuthorizedException e) {
//      log.error("Oauth failed", e);
//    } catch (OAuthExpectationFailedException e) {
//      log.error("Oauth failed", e);
//    } catch (OAuthCommunicationException e) {
//      log.error("Oauth failed", e);
//    }
//    return null;
//  }

  /**
   * Makes a signed OAuth call to twitter at this URL, authed as the current user.
   */
  public String call(String urlAsString) {
    User user = currentUser.getUser();

    return call(user, urlAsString);
  }

  public String call(User user, String urlAsString) {
    consumer.setTokenWithSecret(user.getTwitterAccessToken(), user.getTwitterTokenSecret());

    // create an HTTP request to a protected resource
    URL url;
    try {
      url = new URL(urlAsString);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestProperty("Content-Type", "application/json");

      // sign the request
      consumer.sign(connection);

      // send the request
      connection.connect();

      InputStream inputStream = connection.getInputStream();
      if (connection.getResponseCode() == 200) {
        return IOUtils.toString(inputStream);
      } else {
        log.error("Twitter returned error code {} with message {}", connection.getResponseCode(),
            IOUtils.toString(inputStream));
      }
      IOUtils.closeQuietly(inputStream);

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
