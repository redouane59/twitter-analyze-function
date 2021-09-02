import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.Redouane59.twitter.function.TweetAnalyzer;
import io.github.Redouane59.twitter.model.ActionType;
import io.github.Redouane59.twitter.model.AnalyzeResponse;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import org.junit.jupiter.api.Test;

public class TweetAnalyzerTest {

  @Test
  public void testLikeAnalyse() {
    TweetAnalyzer   tweetAnalyzer = new TweetAnalyzer();
    Tweet           tweet         = tweetAnalyzer.getTwitterClient().getTweet("1035192987008020480");
    AnalyzeResponse response1     = tweetAnalyzer.getFollowersAnalyzer().getTweetAnalyzeResponse(tweet, ActionType.LIKE);
    assertTrue(response1.getMostFollowedInfluencers().size() > 100);
    assertTrue(response1.getMostFollowedInfluencers().get("s_assbague") > 10);
    AnalyzeResponse response2 = tweetAnalyzer.getFollowersAnalyzer().getTweetAnalyzeResponse(tweet, null);
    assertTrue(response2.getMostFollowedInfluencers().size() > 100);
    assertTrue(response2.getMostFollowedInfluencers().get("s_assbague") > 10);
    assertEquals(response2.getMostFollowedInfluencers().get("s_assbague"), response1.getMostFollowedInfluencers().get("s_assbague"));
    AnalyzeResponse response3 = tweetAnalyzer.getFollowersAnalyzer().getTweetAnalyzeResponse(tweet, ActionType.RETWEET);
    assertTrue(response3.getMostFollowedInfluencers().size() > 100);
    assertTrue(response3.getMostFollowedInfluencers().get("s_assbague") > 35);
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
}
