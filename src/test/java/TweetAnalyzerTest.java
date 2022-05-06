import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.Redouane59.twitter.function.TweetAnalyzer;
import io.github.Redouane59.twitter.model.ActionType;
import io.github.Redouane59.twitter.model.AnalyzeResponse;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.tweet.TweetV2.TweetData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TweetAnalyzerTest {

  @Test
  public void testLikeAnalyse() {
    TweetAnalyzer   tweetAnalyzer = new TweetAnalyzer();
    Tweet           tweet         = tweetAnalyzer.getTwitterClient().getTweet("1035192987008020480");
    AnalyzeResponse response1     = tweetAnalyzer.getFollowersAnalyzer().getTweetAnalyzeResponse(tweet, ActionType.LIKE, 100);
    assertTrue(response1.getMostFollowedInfluencers().size() > 100);
    assertTrue(response1.getLikesAnalyzed() > 80);
    assertTrue(response1.getMostFollowedInfluencers().get("s_assbague") > 10);
    AnalyzeResponse response2 = tweetAnalyzer.getFollowersAnalyzer().getTweetAnalyzeResponse(tweet, null, 100);
    assertTrue(response2.getMostFollowedInfluencers().size() > 100);
    assertTrue(response2.getMostFollowedInfluencers().get("s_assbague") > 10);
    assertEquals(response2.getMostFollowedInfluencers().get("s_assbague"), response1.getMostFollowedInfluencers().get("s_assbague"));
    AnalyzeResponse response3 = tweetAnalyzer.getFollowersAnalyzer().getTweetAnalyzeResponse(tweet, ActionType.RETWEET, 100);
    assertTrue(response3.getMostFollowedInfluencers().size() > 100);
    assertTrue(response3.getMostFollowedInfluencers().get("s_assbague") > 15);
    assertNotEquals(response3.getMostFollowedInfluencers().get("s_assbague"), response1.getMostFollowedInfluencers().get("s_assbague"));
  }

  @Test
  public void testHashtagAnalyse() {
    TweetAnalyzer   tweetAnalyzer = new TweetAnalyzer();
    AnalyzeResponse response      = tweetAnalyzer.getFollowersAnalyzer().getHashtagAnalyzeResponse("passsanitaire", ActionType.LIKE);
    assertTrue(response.getMostFollowedInfluencers().size() > 100);
    assertTrue(response.getMostFollowedInfluencers().get("Poulin2012") > 5);
    response = tweetAnalyzer.getFollowersAnalyzer().getHashtagAnalyzeResponse("passsanitaire", null);
    assertTrue(response.getMostFollowedInfluencers().size() > 100);
    assertTrue(response.getMostFollowedInfluencers().get("Poulin2012") > 5);
  }


  @Test
  @Disabled
  public void analyzeAnalyzers() {
    TweetAnalyzer   tweetAnalyzer   = new TweetAnalyzer();
    AnalyzeResponse analyzeResponse = AnalyzeResponse.builder().build();

    TweetList    tweets  = tweetAnalyzer.getTwitterClient().searchTweets("@RedTheBot_ analyse");
    List<String> userIds = new ArrayList<>();
    for (TweetData tweet : tweets.getData()) {
      userIds.add(tweet.getAuthorId());
    }
    LinkedHashMap<String, Integer> mostFollowedInfluencers = tweetAnalyzer.getFollowersAnalyzer().getMostFollowedInfluencers(userIds);
    System.out.println(mostFollowedInfluencers);

    int i        = 0;
    int maxValue = 50;
    for (Map.Entry<String, Integer> entry : mostFollowedInfluencers.entrySet()) {
      if (i < maxValue) {
        int nbFollows  = entry.getValue();
        int percentage = 100 * nbFollows / userIds.size();
        entry.setValue(percentage);
        i++;
      } else {
        break;
      }
    }

    analyzeResponse.setMostFollowedInfluencers(mostFollowedInfluencers);

    System.out.println(mostFollowedInfluencers);

  }

  @Test
  public void testDiv(){
    int nbFollows = 11;
    int nbLikes = 23;
    System.out.println(Math.round(100*nbFollows/(double)nbLikes));
  }
}
