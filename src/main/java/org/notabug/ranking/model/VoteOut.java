package org.notabug.ranking.model;

public record VoteOut(String user, int votes) {
  public static VoteOut from(VoteDynamoDb voteDynamoDb) {
    return new VoteOut(voteDynamoDb.getUser(), voteDynamoDb.getVotes());
  }
}
