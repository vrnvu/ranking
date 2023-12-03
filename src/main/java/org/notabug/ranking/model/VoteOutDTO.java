package org.notabug.ranking.model;

public record VoteOutDTO(String user, int votes) {
  public static VoteOutDTO from(VoteOut votes) {
    return new VoteOutDTO(votes.user(), votes.votes());
  }
}
