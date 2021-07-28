package io.github.Redouane59.twitter.function;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FollowerFilesManager {

  private static final String projectId  = "twitter-analyze-function";
  private static final String bucketName = "influencer_followers";

  public static String getFollowerFileUrl(String userName) {
    try {
      GoogleCredentials
          credentials =
          GoogleCredentials.fromStream(new FileInputStream("src/main/resources/twitter-analyze-function-6f2cb54dc911.json"))
                           .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
      Storage storage = StorageOptions.newBuilder()
                                      .setProjectId(projectId)
                                      .setCredentials(credentials)
                                      .build().getService();
      BlobInfo      blobInfo      = BlobInfo.newBuilder(BlobId.of(bucketName, userName + ".json")).build();
      SignUrlOption signUrlOption = Storage.SignUrlOption.withV4Signature();
      URL           url           = storage.signUrl(blobInfo, 48, TimeUnit.HOURS, signUrlOption); // @todo failing
      return url.toURI().toString();
    } catch (Exception e) {
      LOGGER.error("getFollowerFileUrl exception " + e.getMessage() +
                   "\n" + e.getStackTrace()[0]
                   + "\n" + e.getStackTrace()[1]);
      return null;
    }
  }
}