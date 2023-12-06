package org.notabug.ranking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.notabug.ranking.model.VoteInDTO;
import org.notabug.ranking.model.VoteOut;
import org.notabug.ranking.repository.Storage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RankingServiceTest {
    private RankingService service;
    private Storage storage;

    @BeforeEach
    public void setUp() {
        storage = Mockito.mock(Storage.class);
        service = new RankingService(storage);
    }

    @Test
    public void whenGetAllThen() {
        VoteOut expected = new VoteOut("expected", 1, 1);
        Mockito.when(storage.getAll()).thenReturn(Flux.just(expected));

        StepVerifier
                .create(service.getAll())
                .expectNext(expected)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenVoteAdd() {
        Mockito.when(storage.vote("user", 1, 1)).thenReturn(Mono.empty());

        StepVerifier
                .create(service.vote(new VoteInDTO("user", 1, 1)))
                .expectComplete()
                .verify();
    }
}