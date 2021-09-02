package io.github.Redouane59.twitter.model;

public enum ActionType {

  TWEET("tweet"),
  RETWEET("retweet"),
  LIKE("like"),
  REPLY("reply"),
  QUOTE("quote");

  String value;

  ActionType(String value) {
    this.value = value;
  }

  public static ActionType findByValue(String value) {
    for (ActionType v : values()) {
      if (v.value.equals(value)) {
        return v;
      }
    }
    return null;
  }
}
