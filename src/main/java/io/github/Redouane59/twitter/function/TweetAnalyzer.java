package io.github.Redouane59.twitter.function;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.github.Redouane59.twitter.model.ActionType;
import io.github.Redouane59.twitter.model.AnalyzeResponse;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class TweetAnalyzer implements HttpFunction {

  public final static String TWEET_ID    = "tweet_id";
  public final static String HASHTAG     = "hashtag";
  public final static String ACTION_TYPE = "action_type";
  public final static String MAX_RESULTS = "max_results";

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

    Optional<String> tweetId    = request.getFirstQueryParameter(TWEET_ID);
    ActionType       actionType = ActionType.findByValue(request.getFirstQueryParameter(ACTION_TYPE).orElse(ActionType.LIKE.name()));
    int maxResults = Integer.parseInt(request.getFirstQueryParameter(MAX_RESULTS).orElse("100"));
    if (tweetId.isPresent()) {
      writer.write(analyzeReactions(tweetId.get(), actionType, maxResults));
    } else {
      Optional<String> hashtag = request.getFirstQueryParameter(HASHTAG);
      if (hashtag.isPresent()) {
        writer.write(analyzeHashtag(hashtag.get(), actionType));
      } else {
        LOGGER.error("missing parameters for " + request.getPath());
      }
    }

    LOGGER.debug("finished");

  }

  @SneakyThrows
  public String analyzeReactions(String tweetId, ActionType actionType, int maxResults) {
    Tweet tweet = twitterClient.getTweet(tweetId);
    return FollowersAnalyzer.OBJECT_MAPPER.writeValueAsString(followersAnalyzer.getTweetAnalyzeResponse(tweet, actionType, maxResults));
  }

  @SneakyThrows
  public String analyzeHashtag(String hashtag, ActionType actionType) {
    AnalyzeResponse analyzeResponse = followersAnalyzer.getHashtagAnalyzeResponse(hashtag, actionType);
    return FollowersAnalyzer.OBJECT_MAPPER.writeValueAsString(analyzeResponse);
  }


}

// gcloud functions deploy twitter-analyze-function --entry-point io.github.Redouane59.twitter.function.TweetAnalyzer --runtime java11 --trigger-http --memory 8192MB --timeout=540 --allow-unauthenticated

//.