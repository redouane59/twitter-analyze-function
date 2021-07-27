import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.Redouane59.twitter.function.TweetAnalyzer;
import io.github.redouane59.twitter.dto.user.User;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoadMissingFollowersTest {

  TweetAnalyzer tweetAnalyzer = new TweetAnalyzer();

  @Test
  public void loadMissingFollowers() {

    ObjectMapper mapper = new ObjectMapper();

    List<String> userNames = List.of("GenerationZ_off");
    System.out.println("\n*** STARTING LOADING FOLLOWERS ***\n");
    for (String userName : userNames) {
      try {
        User user = tweetAnalyzer.getTwitterClient().getUserFromUserName(userName);
        System.out.println("anayzing user : " + user.getName());
        List<String> followers = tweetAnalyzer.getTwitterClient().getFollowersIds(user.getId());
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
