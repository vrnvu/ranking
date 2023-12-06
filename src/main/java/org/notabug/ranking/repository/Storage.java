package org.notabug.ranking.repository;

import org.notabug.ranking.model.VoteOut;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Storage {

  Mono<Void> vote(String user, int skill, int toxic);

  Flux<VoteOut> getAll();
}
