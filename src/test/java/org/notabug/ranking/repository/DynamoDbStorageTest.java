package org.notabug.ranking.repository;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.notabug.ranking.model.VoteDynamoDb;
import org.notabug.ranking.model.VoteOut;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;
import java.util.List;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamoDbStorageTest {

    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient =
            DynamoDbEnhancedAsyncClient.builder()
                    .dynamoDbClient(DynamoDbAsyncClient.builder()
                            .region(Region.EU_WEST_1)
                            .endpointOverride(URI.create("http://localhost:8000"))
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "bar")))
                            .build())
                    .build();

    ReactiveCircuitBreakerFactory<?, ?> cbFactory = new ReactiveResilience4JCircuitBreakerFactory(
            CircuitBreakerRegistry.ofDefaults(),
            TimeLimiterRegistry.ofDefaults()
    );

    DynamoDbAsyncTable<VoteDynamoDb> table;
    DynamoDbStorage dynamoDbStorage;

    @BeforeAll
    void setup() {
        table = dynamoDbEnhancedAsyncClient.table(
                VoteDynamoDb.VOTE_TABLE_NAME,
                TableSchema.fromBean(VoteDynamoDb.class)
        );
        table.createTable();
        dynamoDbStorage = new DynamoDbStorage(cbFactory, table);
    }

    @AfterEach
    void recreate() {
        table.deleteTable();
        table.createTable();
    }

    @Test
    public void whenGetAllThenOneVote() {
        StepVerifier.create(dynamoDbStorage.vote("user", 1, 1))
                .verifyComplete();

        StepVerifier.create(dynamoDbStorage.getAll())
                .expectNext(new VoteOut("user", 1, 1))
                .verifyComplete();
    }


    @Test
    public void whenGetAllThenTwoVotes() {
        StepVerifier.create(dynamoDbStorage.vote("a", 1, 1))
                .expectComplete()
                .verify();

        StepVerifier.create(dynamoDbStorage.vote("b", 2, 2))
                .expectComplete()
                .verify();

        List<VoteOut> pendingItems = List.of(
                new VoteOut("a", 1, 1),
                new VoteOut("b", 2, 2)
        );

        StepVerifier.create(dynamoDbStorage.getAll())
                .expectNextCount(pendingItems.size())
                .thenConsumeWhile(pendingItems::contains)
                .verifyComplete();
    }
}