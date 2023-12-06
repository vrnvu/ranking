package org.notabug.ranking.service;

import jakarta.validation.Valid;
import org.notabug.ranking.model.VoteInDTO;
import org.notabug.ranking.model.VoteOut;
import org.notabug.ranking.repository.Storage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RankingService {

  private final Storage storage;

  public RankingService(Storage storage) {
    this.storage = storage;
  }

  public Mono<Void> vote(@Valid VoteInDTO voteInDTO) {
    return storage.vote(voteInDTO.user());
  }

  public Flux<VoteOut> getAll() {
    return storage.getAll();
  }

  public void noTestCoverage() {
    for (int i = 0; i <99; i++) {

    }
  }

}
