package io.github.Redouane59.twitter.function;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.github.Redouane59.twitter.model.InfoBuilder;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TweetAnalyzer implements HttpFunction {

  public final static String TWEET_ID = "tweet_id";

  private       TwitterClient twitterClient;
  private final InfoBuilder   infoBuilder;

  public TweetAnalyzer() {
    File file = new File("../twitter-credentials.json");
    if (!file.exists()) {
      LOGGER.error("credentials file not found");
    }
    try {
      twitterClient = new TwitterClient(TwitterClient.OBJECT_MAPPER.readValue(file, TwitterCredentials.class));
    } catch (IOException e) {
      LOGGER.error("failed reading crendentials file " + e.getMessage());
    }
    infoBuilder = new InfoBuilder(twitterClient);
  }


  @Override
  public void service(HttpRequest request, HttpResponse response) throws Exception {
    BufferedWriter writer = response.getWriter();

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

    writer.write(infoBuilder.getText(tweet));

  }


}