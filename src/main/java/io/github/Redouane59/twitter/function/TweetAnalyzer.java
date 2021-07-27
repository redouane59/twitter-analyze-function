package io.github.Redouane59.twitter.function;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.github.Redouane59.twitter.model.FollowersAnalyzer;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class TweetAnalyzer implements HttpFunction {

  public final static String TWEET_ID = "tweet_id";

  private TwitterClient     twitterClient;
  private FollowersAnalyzer followersAnalyzer;

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
    followersAnalyzer = new FollowersAnalyzer(twitterClient);
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

    writer.write(FollowersAnalyzer.OBJECT_MAPPER.writeValueAsString(followersAnalyzer.getResponse(tweet)));

    LOGGER.debug("finished");

  }


}

// gcloud functions deploy twitter-analyze-function --entry-point io.github.Redouane59.twitter.function.TweetAnalyzer --runtime java11 --trigger-http --memory 8192MB --timeout=540 --allow-unauthenticated