package org.notabug.ranking.model;

public record VoteOut(String user, int skill, int toxic) {
    public static VoteOut from(VoteDynamoDb voteDynamoDb) {
        return new VoteOut(voteDynamoDb.getUser(), voteDynamoDb.getSkill(), voteDynamoDb.getToxic());
    }
}
