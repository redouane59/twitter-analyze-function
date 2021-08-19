package io.github.Redouane59.twitter.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.tweet.TweetV2;
import io.github.redouane59.twitter.dto.tweet.TweetV2.TweetData;
import io.github.redouane59.twitter.dto.user.UserList;
import io.github.redouane59.twitter.dto.user.UserV2.UserData;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FollowersAnalyzer {

  private final       TwitterClient      twitterClient;
  public final static ObjectMapper       OBJECT_MAPPER  = new ObjectMapper();
  public final static List<InfluentUser> INFLUENT_USERS = importInfluentUser();


  public FollowersAnalyzer(TwitterClient twitterClient) {
    this.twitterClient = twitterClient;
  }

  public AnalyzeResponse getLikeAnalyzeResponse(Tweet tweet) {
    AnalyzeResponse analyzeResponse = AnalyzeResponse.builder().tweet(tweet).build();
    if (tweet.getId() == null) {
      return analyzeResponse;
    }

    // likes
    UserList likers = twitterClient.getLikingUsers(tweet.getId());
    if (likers.getData() != null && INFLUENT_USERS.size() > 0) {
      // influenceurs
      LinkedHashMap<String, Integer> mostFollowedInfluencers =
          getMostFollowedInfluencers(likers.getData().stream().map(UserData::getId).collect(Collectors.toList()));
      int           i            = 0;
      int           maxValue     = 10;
      StringBuilder followerText = new StringBuilder();
      followerText.append("Top 10 des personnalités suivies :");
      for (Map.Entry<String, Integer> entry : mostFollowedInfluencers.entrySet()) {
        if (i < maxValue) {
          int nbFollows  = entry.getValue();
          int percentage = 100 * nbFollows / likers.getData().size();
          entry.setValue(percentage);
          i++;
        } else {
          break;
        }
      }

      analyzeResponse.setMostFollowedInfluencers(mostFollowedInfluencers);
      analyzeResponse.setUserStatistics(UserStatisticsCollector.getUserStatistics(likers.getData()));
    }

    return analyzeResponse;
  }

  public AnalyzeResponse getHashtagAnalyzeResponse(String hashtag) {
    AnalyzeResponse
        analyzeResponse =
        AnalyzeResponse.builder().tweet(TweetV2.builder().data(TweetData.builder().text(hashtag).build()).build()).build();
    if (hashtag == null || hashtag.isEmpty()) {
      return analyzeResponse;
    }
    hashtag = hashtag.replace("#", "");
    // tweets
    TweetList
        tweetList =
        twitterClient.searchTweets("#" + hashtag + " -is:retweet", AdditionalParameters.builder().recursiveCall(false).maxResults(100).build());

    if (tweetList.getData() != null && INFLUENT_USERS.size() > 0) {
      List<String> hashtagUserIds = tweetList.getData().stream().map(TweetData::getAuthorId).collect(Collectors.toList());

      // influenceurs
      LinkedHashMap<String, Integer> mostFollowedInfluencers = getMostFollowedInfluencers(hashtagUserIds);
      int                            i                       = 0;
      int                            maxValue                = 10;
      StringBuilder                  followerText            = new StringBuilder();
      followerText.append("Top 10 des personnalités suivies :");
      for (Map.Entry<String, Integer> entry : mostFollowedInfluencers.entrySet()) {
        if (i < maxValue) {
          int nbFollows  = entry.getValue();
          int percentage = 100 * nbFollows / hashtagUserIds.size();
          entry.setValue(percentage);
          i++;
        } else {
          break;
        }
      }

      analyzeResponse.setMostFollowedInfluencers(mostFollowedInfluencers);
      // no userstatistics here because user object empty in searches
    }

    return analyzeResponse;
  }

  private static <K, V> void orderByValue(LinkedHashMap<K, V> m) {
    List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());
    m.clear();
    entries.stream()
           .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.comparingInt(o -> (int) o).reversed()))
           .forEachOrdered(e -> m.put(e.getKey(), e.getValue()));
  }

  public LinkedHashMap<String, Integer> getMostFollowedInfluencers(List<String> userIds) {
    LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
    for (InfluentUser influentUser : INFLUENT_USERS) {
      if (influentUser.getId().isEmpty()) {
        String userId = twitterClient.getUserFromUserName(influentUser.getName()).getId();
        influentUser.getData().setId(userId);
        LOGGER.warn("missing id for " + influentUser.getName() + " -> " + userId);
      }
      int followerCount = 0;
      for (String userId : userIds) {
        if (influentUser.isFollowedByUser(userId)) {
          followerCount++;
        }
      }
      result.put(influentUser.getName(), followerCount);
    }
    orderByValue(result);
    return result;
  }

  public static List<InfluentUser> importInfluentUser() {
    LOGGER.info("importInfluentUser()");
    File file = new File("src/main/resources/influents_users.json");
    if (file.exists()) {
      try {
        return List.of(OBJECT_MAPPER.readValue(file, InfluentUser[].class));
      } catch (Exception e) {
        LOGGER.error(" user importation KO ! " + e.getMessage());
      }
    } else {
      LOGGER.error("file not found");
    }
    return new ArrayList<>();
  }
}
