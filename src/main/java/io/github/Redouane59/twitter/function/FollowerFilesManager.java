package io.github.Redouane59.twitter.function;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FollowerFilesManager {

  private static final String projectId  = "twitter-analyze-function";
  private static final String bucketName = "influencer_followers";

  public static String getFollowerFileUrl(String userName) {
    Storage  storage  = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, userName + ".json")).build();
    URL      url      = storage.signUrl(blobInfo, 24, TimeUnit.HOURS, Storage.SignUrlOption.withV4Signature());
    try {
      return url.toURI().toString();
    } catch (URISyntaxException e) {
      LOGGER.error(e.getMessage());
      return null;
    }
  }
}