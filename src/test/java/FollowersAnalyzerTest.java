import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.Redouane59.twitter.function.TweetAnalyzer;
import io.github.Redouane59.twitter.model.AnalyzeResponse;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import org.junit.jupiter.api.Test;

public class FollowersAnalyzerTest {

  @Test
  public void testLikeAnalyse() {
    TweetAnalyzer   tweetAnalyzer = new TweetAnalyzer();
    Tweet           tweet         = tweetAnalyzer.getTwitterClient().getTweet("1427379729171165186");
    AnalyzeResponse response      = tweetAnalyzer.getFollowersAnalyzer().getLikeAnalyzeResponse(tweet);
    assertTrue(response.getMostFollowedInfluencers().size() > 100);
    assertTrue(response.getMostFollowedInfluencers().get("s_assbague") > 0);
  }

  @Test
  public void testHashtagAnalyse() {
    TweetAnalyzer   tweetAnalyzer = new TweetAnalyzer();
    AnalyzeResponse response      = tweetAnalyzer.getFollowersAnalyzer().getHashtagAnalyzeResponse("passsanitaire");
    assertTrue(response.getMostFollowedInfluencers().size() > 100);
    assertTrue(response.getMostFollowedInfluencers().get("Poulin2012") > 0);
  }
}
