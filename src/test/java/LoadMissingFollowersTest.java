import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoadMissingFollowersTest {

  private TwitterClient
      twitterClient =
      new TwitterClient(TwitterClient.OBJECT_MAPPER.readValue(new File("src/main/resources/test-twitter-credentials.json"),
                                                              TwitterCredentials.class));

  public LoadMissingFollowersTest() throws IOException {
  }

  @Test
  public void loadMissingFollowers() {

    ObjectMapper mapper = new ObjectMapper();

    List<String> userNames = List.of("JM_Bigard");
    System.out.println("\n*** STARTING LOADING FOLLOWERS ***\n");
    for (String userName : userNames) {
      try {
        User user = twitterClient.getUserFromUserName(userName);
        System.out.println("anayzing user : " + user.getName() + " id:" + user.getId());
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

}
