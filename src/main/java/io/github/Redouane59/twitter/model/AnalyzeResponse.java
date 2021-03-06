package io.github.Redouane59.twitter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import java.util.LinkedHashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AnalyzeResponse {

  LinkedHashMap<String, Integer> mostFollowedInfluencers;
  private UserStatistics userStatistics;
  @JsonIgnore
  private Tweet          tweet;
  private int            hashtagsCount;
  private int            likesAnalyzed;

}
