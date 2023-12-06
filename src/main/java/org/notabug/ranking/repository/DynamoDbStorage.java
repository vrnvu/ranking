package org.notabug.ranking.repository;

import org.notabug.ranking.model.VoteDynamoDb;
import org.notabug.ranking.model.VoteOut;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;

public class DynamoDbStorage implements Storage {
    private final ReactiveCircuitBreakerFactory<?, ?> cbFactory;
    private final DynamoDbAsyncTable<VoteDynamoDb> table;

    public DynamoDbStorage(
            ReactiveCircuitBreakerFactory<?, ?> cbFactory,
            DynamoDbAsyncTable<VoteDynamoDb> table) {
        this.cbFactory = cbFactory;
        this.table = table;
    }

    @Override
    public Mono<Void> vote(String user, int skill, int toxic) {
        VoteDynamoDb voteDynamoDb = new VoteDynamoDb(user, skill, toxic);

        return cbFactory.create("Ranking_Vote")
                .run(Mono.fromFuture(table.putItem(voteDynamoDb)))
                .then();
    }

    @Override
    public Flux<VoteOut> getAll() {
        return cbFactory.create("Ranking_GetAll")
                .run(Flux.from(table.scan().items()))
                .map(VoteOut::from);
    }
}
