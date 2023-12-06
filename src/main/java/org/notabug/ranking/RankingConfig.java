package org.notabug.ranking;

import org.notabug.ranking.model.VoteDynamoDb;
import org.notabug.ranking.repository.DynamoDbStorage;
import org.notabug.ranking.repository.Storage;
import org.notabug.ranking.service.RankingService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@AutoConfigureAfter(ReactiveResilience4JAutoConfiguration.class)
@Configuration
public class RankingConfig {

    @Profile("!test")
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        AwsBasicCredentials creds =
                AwsBasicCredentials.create("foo", "bar");
        return StaticCredentialsProvider.create(creds);
    }

    @Profile("!test")
    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient(AwsCredentialsProvider awsCredentialsProvider) {
        return DynamoDbAsyncClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    @Bean
    public DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient(DynamoDbAsyncClient dynamoDbAsyncClient) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(dynamoDbAsyncClient)
                .build();
    }

    @Bean
    public RankingService rankingService(Storage storage) {
        return new RankingService(storage);
    }

    @Bean
    public DynamoDbAsyncTable<VoteDynamoDb> table(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
        return dynamoDbEnhancedAsyncClient.table(
                VoteDynamoDb.VOTE_TABLE_NAME,
                TableSchema.fromBean(VoteDynamoDb.class)
        );
    }

    @Bean
    public Storage storage(
            ReactiveCircuitBreakerFactory<?, ?> cbFactory,
            DynamoDbAsyncTable<VoteDynamoDb> table) {
        return new DynamoDbStorage(cbFactory, table);
    }
}
