package org.notabug.ranking.model;

import java.util.Objects;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbAtomicCounter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class VoteDynamoDb {
  public static final String VOTE_TABLE_NAME = "ranking_votes";
  public static final String USER_ATTRIBUTE = "user";
  public static final String VOTES_ATTRIBUTE = "votes";

  private String user;
  private int votes;

  public VoteDynamoDb() {
  }

  public VoteDynamoDb(String user, int votes) {
    this.user = user;
    this.votes = votes;
  }

  @DynamoDbPartitionKey
  @DynamoDbAttribute(USER_ATTRIBUTE)
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @DynamoDbAttribute(VOTES_ATTRIBUTE)
  @DynamoDbAtomicCounter
  public int getVotes() {
    return votes;
  }

  public void setVotes(int votes) {
    this.votes = votes;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VoteDynamoDb voteDynamoDb1 = (VoteDynamoDb) o;
    return votes == voteDynamoDb1.votes && Objects.equals(user, voteDynamoDb1.user);
  }

  @Override public int hashCode() {
    return Objects.hash(user, votes);
  }
}
