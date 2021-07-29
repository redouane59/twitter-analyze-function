import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.Redouane59.twitter.function.FollowerFilesManager;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class FollowerFilesManagerTest {

  private String userName = "RNational_off";

  @Test
  public void testGetObectUrl() throws URISyntaxException {
    URL result = FollowerFilesManager.getFollowerFileUrl(userName);
    System.out.println(result.toURI().toString());
    assertNotNull(result);
  }

}
