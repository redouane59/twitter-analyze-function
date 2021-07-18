package io.github.Redouane59.twitter.model;

import io.github.Redouane59.twitter.function.TweetAnalyzer;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.dto.user.UserList;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseBuilder {

  private final TwitterClient twitterClient;

  public ResponseBuilder(TwitterClient twitterClient) {
    this.twitterClient = twitterClient;
  }

  public AnalyzeResponse getResponse(Tweet tweet) {
    AnalyzeResponse analyzeResponse = AnalyzeResponse.builder().tweet(tweet).build();

    // likes
    UserList likers = twitterClient.getLikingUsers(tweet.getId());
    if (likers.getData() != null && TweetAnalyzer.INFLUENT_USERS.size() > 0) {
      // influenceurs
      LinkedHashMap<String, Integer> mostFollowedInfluencers = getMostFollowedInfluencers(likers.getData());
      int                            i                       = 0;
      int                            maxValue                = 10;
      StringBuilder                  followerText            = new StringBuilder();
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
    
        /*
    // retweets
    List<String> retweeterIds = twitterClient.getRetweetersId(tweet.getId());
    text += getTextFromUserList(twitterClient.getUsersFromUserIds(retweeterIds), "Retweeting users");
    text += "\n";

    // mentions
    List<? extends User> answerers = searchForAnswerers(tweet);
    text += getTextFromUserList(answerers, "Answering users");
    text += "\n";
     */

    return analyzeResponse;
  }

  private static <K, V> void orderByValue(LinkedHashMap<K, V> m) {
    List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());
    m.clear();
    entries.stream()
           .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.comparingInt(o -> (int) o).reversed()))
           .forEachOrdered(e -> m.put(e.getKey(), e.getValue()));
  }


  private LinkedHashMap<String, Integer> getMostFollowedInfluencers(List<? extends User> users) {
    LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
    for (InfluentUser influentUser : TweetAnalyzer.INFLUENT_USERS) {
      if (influentUser.getId().isEmpty()) {
        String userId = twitterClient.getUserFromUserName(influentUser.getName()).getId();
        influentUser.getData().setId(userId);
        LOGGER.warn("missing id for " + influentUser.getName() + " -> " + userId);
      }
      int followerCount = 0;
      for (User user : users) {
        if (influentUser.isFollowedByUser(user.getId())) {
          followerCount++;
        }
      }
      result.put(influentUser.getName(), followerCount);
    }
    orderByValue(result);
    return result;

  }

  private String getTextFromUserList(List<? extends User> users, String methodName) {
    String         text           = "";
    UserStatistics userStatistics = UserStatisticsCollector.getUserStatistics(users);
    text += "\n[" + methodName + "] median tweets count : " + userStatistics.getTweetCountMedian();
    text += "\n[" + methodName + "] median followers count : " + userStatistics.getFollowersCountMedian();
    text += "\n[" + methodName + "] median following count : " + userStatistics.getFollowingsCountMedian();
    text += "\n[" + methodName + "] median ratio : " + new DecimalFormat("##.##").format(userStatistics.getFollowerRatioMedian());
    text += "\n[" + methodName + "] median accounts age (years) : " + new DecimalFormat("##.##").format(userStatistics.getAccountAgeMedian());
    return text;
  }

  private List<? extends User> searchForAnswerers(Tweet tweet) {
    List<User> users  = new ArrayList<>();
    String     query  = "conversation_id:" + tweet.getId() + " to:" + tweet.getUser().getName();
    TweetList  result = twitterClient.searchTweets(query);
    for (Tweet t : result.getData()) {
      User user = twitterClient.getUserFromUserId(t.getAuthorId());
      users.add(user);
    }
    return users;
  }
}
