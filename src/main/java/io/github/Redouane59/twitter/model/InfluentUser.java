package io.github.Redouane59.twitter.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.Redouane59.twitter.function.FollowerFilesManager;
import io.github.redouane59.RelationType;
import io.github.redouane59.twitter.dto.user.UserV2;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(using = InfluentUser.InfluentUserDeserializer.class)
@JsonSerialize(using = InfluentUser.InfluentUserSerializer.class)
@Slf4j
public class InfluentUser extends UserV2 {


  private              HashSet<String> followerIds   = new HashSet<>();
  private final static ObjectMapper    OBJECT_MAPPER = new ObjectMapper();
  private static final String          LOCAL_PATH    = "src/main/resources/users/followers/";

  public InfluentUser(String name, String id) {
    setData(UserData.builder().id(id).name(name).build());
    loadFollowerIds();
  }

  private void loadFollowerIds() {
    File file = new File(LOCAL_PATH + getName() + ".json");
    if (file.exists()) {
      loadFollowerOffline(file);
    } else {
      loadFollowersOnlineFromStorage();
    }
  }

  private void loadFollowerOffline(File file) {
    try {
      LOGGER.info("loading followers offline to : " + file.getName());
      followerIds = new HashSet<>(List.of(OBJECT_MAPPER.readValue(file, String[].class)));
    } catch (IOException ioException) {
      LOGGER.error(ioException.getMessage());
    }
  }

  private void loadFollowersOnlineFromStorage() {
    try {
      LOGGER.info("loading followers from storage for " + getName());
      followerIds = new HashSet<>();
      URL followerUrl = FollowerFilesManager.getFollowerFileUrl(getName());
      followerIds = OBJECT_MAPPER.readValue(followerUrl, HashSet.class);
      return;
    } catch (Exception exception) {
      LOGGER.error("failed downloading " + getName() + ".json : " + exception.getMessage());
      loadFollowersOnlineFromGitHub();
    }
  }

  private void loadFollowersOnlineFromGitHub() {
    LOGGER.info("loading followers from GitHub for " + getName());
    followerIds = new HashSet<>();
    String jsonUrl = "https://github.com/redouane59/twitter-accounts-data/raw/master/users/followers/" + getName() + ".json";
    try {
      followerIds = OBJECT_MAPPER.readValue(new URL(jsonUrl), HashSet.class);
      LOGGER.info(getName() + " loaded online with success");
    } catch (IOException ioException) {
      LOGGER.error("failed loading " + getName() + " : " + ioException.getMessage());
    }
  }

  public boolean isFollowedByUser(String userId) {
    return followerIds.contains(userId);
  }

  public RelationType getPartialRelationType(String userId) {
    if (isFollowedByUser(userId)) {
      return RelationType.FOLLOWING;
    } else {
      return RelationType.NONE;
    }
  }

  public static class InfluentUserDeserializer extends StdDeserializer<InfluentUser> {

    public InfluentUserDeserializer() {
      this(null);
    }

    public InfluentUserDeserializer(Class<?> vc) {
      super(vc);
    }

    @Override
    public InfluentUser deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
      JsonNode node = jp.getCodec().readTree(jp);
      String   name = node.get("name").asText();
      String   id   = node.get("id").asText();
      return new InfluentUser(name, id);
    }
  }

  public static class InfluentUserSerializer extends StdSerializer<InfluentUser> {

    public InfluentUserSerializer() {
      this(null);
    }

    public InfluentUserSerializer(Class<InfluentUser> t) {
      super(t);
    }

    @Override
    public void serialize(
        InfluentUser user, JsonGenerator jgen, SerializerProvider provider)
    throws IOException {

      jgen.writeStartObject();
      jgen.writeStringField("name", user.getName());
      jgen.writeStringField("id", user.getId());
      jgen.writeEndObject();
    }
  }


}


