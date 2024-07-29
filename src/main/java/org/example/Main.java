package org.example;

import twitter4j.Status;
import twitter4j.Twitter;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.util.Properties;

public class Main {
    static Twitter twitter;
    static AccessToken token;

    static Properties properties;

    public static void main(String[] args) throws TwitterException, IOException {
        loadProperties();
        ConfigurationBuilder cb = getBuilder();
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        try {
            System.out.println(createTweet("Hello World"));
        } catch (TwitterException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadProperties() throws IOException {
        Properties property = new Properties();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("twitter4j.properties");
        property.load(is);

        Set<Object> keySet = property.keySet();
        String key;
        String value;

        for (Object obj : keySet) {
            key = obj.toString();
            value = property.getProperty(key);
            property.setProperty(key, value);
        }
        properties = property;
    }

    private static ConfigurationBuilder getBuilder() throws TwitterException {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(properties.getProperty("oauth.consumerKey"))
                .setOAuthConsumerSecret(properties.getProperty("oauth.consumerSecret"))
                .setOAuthAccessToken(properties.getProperty("oauth.accessToken"))
                .setOAuthAccessTokenSecret(properties.getProperty("oauth.accessTokenSecret"));

        return cb;
    }


    public static String createTweet(String tweet) throws TwitterException, URISyntaxException, IOException {
        authenticate();
        twitter.setOAuthAccessToken(new AccessToken(properties.getProperty("oauth.accessToken"), properties.getProperty("oauth.accessTokenSecret")));
        Status status = twitter.updateStatus(tweet);

        return status.getText();
    }

    private static void authenticate() throws TwitterException, URISyntaxException, IOException {
        AccessToken accessToken = null;
        RequestToken requestToken = twitter.getOAuthRequestToken();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (accessToken == null) {
            System.out.println(requestToken.getToken());
            Desktop.getDesktop().browse(
                    new URI(requestToken.getAuthorizationURL()));

            System.out
                    .print("\nEnter the PIN authorization acquired in the application\n"
                            + "by the browser and press enter:\n");

            String pin = br.readLine();
            try {
                if (pin.length() > 0) {
                    accessToken = twitter.getOAuthAccessToken(
                            requestToken, pin);

                } else {
                    accessToken = twitter
                            .getOAuthAccessToken(requestToken);
                }
            } catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    System.out.println("Unable to get the access token.");
                } else {
                    te.printStackTrace();
                }
            }
        }

        properties.setProperty("oauth.accessToken", accessToken.getToken());
        properties.setProperty("oauth.accessTokenSecret",
                accessToken.getTokenSecret());

    }
        //twitter.setOAuthAccessToken(new AccessToken(properties.getProperty("oauth.accessToken"), properties.getProperty("oauth.accessTokenSecret")));
    }