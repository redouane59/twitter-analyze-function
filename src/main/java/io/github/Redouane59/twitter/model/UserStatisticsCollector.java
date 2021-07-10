package io.github.Redouane59.twitter.model;

import io.github.redouane59.twitter.dto.user.User;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.DoubleStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserStatisticsCollector {

  public static UserStatistics getUserStatistics(List<? extends User> users) {

    if (users.size() <= 0) {
      LOGGER.error("no user found to build user statistics");
      return UserStatistics.builder().build();
    }

    DoubleStream sortedFollowersCounts = users.stream().mapToDouble(User::getFollowersCount).sorted();
    double followersCountMedian = users.size() % 2 == 0 ?
                                  sortedFollowersCounts.skip(users.size() / 2 - 1).limit(2).average().getAsDouble() :
                                  sortedFollowersCounts.skip(users.size() / 2).findFirst().getAsDouble();

    DoubleStream sortedFollowingsCounts = users.stream().mapToDouble(User::getFollowingCount).sorted();
    double followingsCountMedian = users.size() % 2 == 0 ?
                                   sortedFollowingsCounts.skip(users.size() / 2 - 1).limit(2).average().getAsDouble() :
                                   sortedFollowingsCounts.skip(users.size() / 2).findFirst().getAsDouble();

    DoubleStream sortedRatioCounts = users.stream().mapToDouble(u -> u.getFollowersCount() / (double) (1 + u.getFollowingCount())).sorted();
    double ratioMedian = users.size() % 2 == 0 ?
                         sortedRatioCounts.skip(users.size() / 2 - 1).limit(2).average().getAsDouble() :
                         sortedRatioCounts.skip(users.size() / 2).findFirst().getAsDouble();

    DoubleStream sortedTweetCounts = users.stream().mapToDouble(User::getTweetCount).sorted();
    double tweetCountMedian = users.size() % 2 == 0 ?
                              sortedTweetCounts.skip(users.size() / 2 - 1).limit(2).average().getAsDouble() :
                              sortedTweetCounts.skip(users.size() / 2).findFirst().getAsDouble();

    DoubleStream sortedCreatedAt = users.stream().mapToDouble(u -> ChronoUnit.SECONDS.between(u.getDateOfCreation(), LocalDateTime.now())).sorted();
    double accountAgeSeconds = users.size() % 2 == 0 ?
                               sortedCreatedAt.skip(users.size() / 2 - 1).limit(2).average().getAsDouble() :
                               sortedCreatedAt.skip(users.size() / 2).findFirst().getAsDouble();

    return UserStatistics.builder()
                         .followersCountMedian((int) followersCountMedian)
                         .followingsCountMedian((int) followingsCountMedian)
                         .followerRatioMedian(ratioMedian)
                         .tweetCountMedian((int) tweetCountMedian)
                         .accountAgeMedian(accountAgeSeconds / (double) (60 * 60 * 24 * 365))
                         .build();
  }

}
