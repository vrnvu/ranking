package org.notabug.ranking.controller;

import jakarta.validation.Valid;
import org.notabug.ranking.model.VoteInDTO;
import org.notabug.ranking.model.VoteOutDTO;
import org.notabug.ranking.service.RankingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.notabug.ranking.core.HttpHeaders.REQUEST_ID_HEADER;

@RestController
@RequestMapping("/ranking")
public class RankingController {

  private final RankingService service;

  public RankingController(RankingService service) {
    this.service = service;
  }

  @PutMapping(value = "/vote/{voteInDTO}", produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
  public Mono<Void> putVote(
      @RequestHeader(name = REQUEST_ID_HEADER, required = false, defaultValue = "") String requestId,
      @Valid @PathVariable VoteInDTO voteInDTO) {
    return service.vote(voteInDTO);
  }

  @GetMapping(value = "/all", produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
  public Flux<VoteOutDTO> getAll(
      @RequestHeader(name = REQUEST_ID_HEADER, required = false, defaultValue = "") String requestId
  ) {
    return service.getAll().map(VoteOutDTO::from);
  }
}
