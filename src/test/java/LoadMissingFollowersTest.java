import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.Redouane59.twitter.model.FollowersAnalyzer;
import io.github.Redouane59.twitter.model.InfluentUser;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.dto.user.UserV2;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
@Disabled
public class LoadMissingFollowersTest {

  private TwitterClient
      twitterClient =
      new TwitterClient(TwitterClient.OBJECT_MAPPER.readValue(new File("../twitter-credentials - RBA.json"),
                                                              TwitterCredentials.class));

  public LoadMissingFollowersTest() throws IOException {
  }

  @Test
  @Disabled
  public void loadMissingFollowers() {

    ObjectMapper mapper = new ObjectMapper();

    List<String> userNames = List.of("julienbayou", "UPR_Officiel", "Etienne_Chouard");
    System.out.println("\n*** STARTING LOADING FOLLOWERS ***\n");
    for (String userName : userNames) {
      try {
        User user = twitterClient.getUserFromUserName(userName);
        System.out.println("anayzing user : " + user.getName() + " / " + user.getId());
        List<String> followers = twitterClient.getFollowersIds(user.getId());
        assertTrue(followers.size() > 0);
        File destFile = new File("../twitter-analyze-function/src/main/resources/users/followers/" + user.getName() + ".json");
        mapper.writeValue(destFile, followers);
        assertTrue(destFile.exists());
      } catch (Exception e) {
        LOGGER.error(e.getMessage());
      }
    }
  }

  // @todo add test to check if the follower count is OK
  @Test
  @Disabled
  public void testInfluentUserCount() {
    FollowersAnalyzer followersAnalyzer = new FollowersAnalyzer(twitterClient);
    System.out.println("");
    for (InfluentUser influentUser : followersAnalyzer.INFLUENT_USERS) {
      boolean reliable = isFollowersIdsDataReliable(influentUser);
    }
    System.out.println("");
  }

  private boolean isFollowersIdsDataReliable(InfluentUser influencer) {
    int offlineFollowersCount = influencer.getFollowerIds().size();
    if (offlineFollowersCount == 0) {
      LOGGER.error("No offline followers found for " + influencer.getName());
      return false;
    }
    User user = twitterClient.getUserFromUserName(influencer.getName());
    if (user == null || ((UserV2) user).getData() == null) {
      LOGGER.error(influencer.getName() + " not found !");
      return false;
    }
    int    apiFollowersCount = user.getFollowersCount();
    int    diff              = Math.abs(apiFollowersCount - offlineFollowersCount);
    double acceptableDelta   = 0.1;
    if (diff > acceptableDelta * apiFollowersCount) {
      LOGGER.error("/!\\ Follower ids data doesn't look accurate for "
                   + influencer.getName()
                   + " ("
                   + offlineFollowersCount
                   + " ids VS "
                   + apiFollowersCount
                   + " followers ");
      return false;
    } else {
      LOGGER.debug("followers ids OK for " + influencer.getName());
      return true;
    }
  }

}
