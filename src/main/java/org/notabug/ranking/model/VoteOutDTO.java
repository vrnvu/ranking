package org.notabug.ranking.model;

public record VoteOutDTO(String user, int skill, int toxic) {
  public static VoteOutDTO from(VoteOut votes) {
    return new VoteOutDTO(votes.user(), votes.skill(), votes.toxic());
  }
}
