package org.notabug.ranking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.notabug.ranking.TestConfig;
import org.notabug.ranking.model.VoteDynamoDb;
import org.notabug.ranking.model.VoteOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamoDbStorageTest {

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    @Autowired
    ReactiveCircuitBreakerFactory<?, ?> cbFactory;

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

    @Test
    public void whenGetAllThenOneVote() {
        table.deleteTable();
        table.createTable();

        StepVerifier.create(dynamoDbStorage.vote("user", 1, 1))
                .verifyComplete();

        StepVerifier.create(dynamoDbStorage.getAll())
                .expectNext(new VoteOut("user", 1, 1))
                .verifyComplete();
    }


    @Test
    public void whenGetAllThenTwoVotes() {
        table.deleteTable();
        table.createTable();

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