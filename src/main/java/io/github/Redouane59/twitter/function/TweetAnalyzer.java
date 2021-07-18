package io.github.Redouane59.twitter.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.github.Redouane59.twitter.model.InfluentUser;
import io.github.Redouane59.twitter.model.ResponseBuilder;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TweetAnalyzer implements HttpFunction {

  public final static String TWEET_ID = "tweet_id";

  private              TwitterClient      twitterClient;
  private              ResponseBuilder    responseBuilder;
  private final static ObjectMapper       OBJECT_MAPPER  = new ObjectMapper();
  public static final  List<InfluentUser> INFLUENT_USERS = importInfluentUser("influents_users.json");

  public TweetAnalyzer() {

    File file = new File("src/main/resources/test-twitter-credentials.json");
    if (!file.exists()) {
      LOGGER.error("credentials file not found");
      return;
    }
    try {
      twitterClient = new TwitterClient(TwitterClient.OBJECT_MAPPER.readValue(file, TwitterCredentials.class));
      LOGGER.info("twitterClient initialized with success");
    } catch (IOException e) {
      LOGGER.error("failed reading crendentials file " + e.getMessage());
    }
    responseBuilder = new ResponseBuilder(twitterClient);
  }

  public static List<InfluentUser> importInfluentUser(String userName) {
    File file = new File("src/main/resources/" + userName);
    if (file.exists()) {
      try {
        return List.of(OBJECT_MAPPER.readValue(file, InfluentUser[].class));
      } catch (Exception e) {
        LOGGER.error(" user importation KO ! " + e.getMessage());
      }
    } else {
      LOGGER.error("file not found");
    }
    return null;
  }

  @Override
  public void service(HttpRequest request, HttpResponse response) throws Exception {
    BufferedWriter writer = response.getWriter();

    LOGGER.debug("params:");
    for (Entry<String, List<String>> entry : request.getQueryParameters().entrySet()) {
      LOGGER.info("key " + entry.getKey());
      if (entry.getValue() != null && entry.getValue().size() > 0) {
        LOGGER.info("value " + entry.getValue().get(0));
      }
    }

    Optional<String> tweetId = request.getFirstQueryParameter(TWEET_ID);
    if (tweetId.isEmpty()) {
      writer.write("ERROR : tweet_id parameter missing");
      return;
    }

    Tweet tweet = twitterClient.getTweet(tweetId.get());

    if (tweet.getId() == null) {
      writer.write("ERROR : tweet not found");
      return;
    }

    writer.write(OBJECT_MAPPER.writeValueAsString(responseBuilder.getResponse(tweet)));

    LOGGER.debug("finished");

  }


}

// gcloud functions deploy twitter-analyze-function --entry-point io.github.Redouane59.twitter.function.TweetAnalyzer --runtime java11 --trigger-http --memory 4096MB --timeout=540 --allow-unauthenticated