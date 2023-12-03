package org.notabug.ranking.repository;

import org.notabug.ranking.model.VoteOut;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Storage {

  Mono<Void> increment(String user);

  Flux<VoteOut> getAll();
}
