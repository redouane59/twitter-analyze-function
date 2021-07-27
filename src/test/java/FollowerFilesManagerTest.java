import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.Redouane59.twitter.function.FollowerFilesManager;
import org.junit.jupiter.api.Test;

public class FollowerFilesManagerTest {

  private String objectName = "__iron_man_.json";

  @Test
  public void testGetObectUrl() {
    String result = FollowerFilesManager.getFollowerFileUrl(objectName);
    System.out.println(result);
    assertNotNull(result);
  }

}
