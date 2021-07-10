package io.github.Redouane59.twitter.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserStatistics {

  private int    followersCountMedian;
  private int    followingsCountMedian;
  private double followerRatioMedian;
  private int    tweetCountMedian;
  // year
  private double accountAgeMedian;

}
