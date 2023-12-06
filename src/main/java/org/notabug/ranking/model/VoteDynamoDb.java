package org.notabug.ranking.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class VoteDynamoDb {
    public static final String VOTE_TABLE_NAME = "ranking_votes";
    public static final String USER_ATTRIBUTE = "user";
    public static final String SKILL_ATTRIBUTE = "skill";
    public static final String TOXIC_ATTRIBUTE = "toxic";

    private String user;
    private int skill;
    private int toxic;

    public VoteDynamoDb() {
    }

    public VoteDynamoDb(String user, int skill, int toxic) {
        this.user = user;
        this.skill = skill;
        this.toxic = toxic;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute(USER_ATTRIBUTE)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @DynamoDbAttribute(SKILL_ATTRIBUTE)
    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    @DynamoDbAttribute(TOXIC_ATTRIBUTE)
    public int getToxic() {
        return toxic;
    }

    public void setToxic(int toxic) {
        this.toxic = toxic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VoteDynamoDb that = (VoteDynamoDb) o;

        if (skill != that.skill) return false;
        if (toxic != that.toxic) return false;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + skill;
        result = 31 * result + toxic;
        return result;
    }
}
