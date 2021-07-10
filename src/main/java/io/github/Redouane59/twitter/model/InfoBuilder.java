package io.github.Redouane59.twitter.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.dto.user.UserList;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfoBuilder {

  private final        TwitterClient      twitterClient;
  private final        List<InfluentUser> influentUsers;
  private final static ObjectMapper       OBJECT_MAPPER = new ObjectMapper();

  public InfoBuilder(TwitterClient twitterClient) {
    this.twitterClient = twitterClient;
    influentUsers      = importInfluentUser("influents_users.json");
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
      LOGGER.error("file not foun");
    }
    return null;
  }

  public String getText(Tweet tweet) {
    StringBuilder text = new StringBuilder("*** Analyzing tweet " + tweet.getId() + " ***\n");
    text.append("\n[Tweet] author : @").append(twitterClient.getUserFromUserId(tweet.getAuthorId()).getName());
    text.append("\n[Tweet] text : ").append(tweet.getText());
    text.append("\n[Tweet] reply_count : ").append(tweet.getReplyCount());
    text.append("\n[Tweet] like_count : ").append(tweet.getLikeCount());
    text.append("\n[Tweet] retweet_count : ").append(tweet.getRetweetCount());
    text.append("\n");

    // likes
    UserList likers = twitterClient.getLikingUsers(tweet.getId());
    if (likers.getData() != null) {
      // influenceurs
      LinkedHashMap<InfluentUser, Integer> mostFollowedInfluencers = getMostFollowedInfluencers(likers.getData());
      int                                  i                       = 0;
      int                                  maxValue                = 15;
      text.append("\n\n[Most followed famous accounts by liking users]");
      for (Map.Entry<InfluentUser, Integer> entry : mostFollowedInfluencers.entrySet()) {
        if (i < maxValue) {
          text.append("\n@").append(entry.getKey().getName()).append(" -> ").append(entry.getValue());
          i++;
        } else {
          break;
        }
      }

      text.append("\n" + getTextFromUserList(likers.getData(), "Liking users"));
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

    return text.toString();
  }

  private static <K, V> void orderByValue(LinkedHashMap<K, V> m) {
    List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());
    m.clear();
    entries.stream()
           .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.comparingInt(o -> (int) o).reversed()))
           .forEachOrdered(e -> m.put(e.getKey(), e.getValue()));
  }


  private LinkedHashMap<InfluentUser, Integer> getMostFollowedInfluencers(List<? extends User> users) {
    LinkedHashMap<InfluentUser, Integer> result = new LinkedHashMap<>();
    for (InfluentUser influentUser : influentUsers) {
      int followerCount = 0;
      for (User user : users) {
        if (influentUser.isFollowedByUser(user.getId())) {
          followerCount++;
        }
      }
      result.put(influentUser, followerCount);
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
