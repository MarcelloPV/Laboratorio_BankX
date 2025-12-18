package com.example.demo.api.controller;

import com.example.demo.api.dto.CreateTxRequest;
import com.example.demo.domain.mongo.Transaction;
import com.example.demo.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService service;

  @PostMapping("/transactions")
  public Mono<ResponseEntity<Transaction>> create(@Valid @RequestBody CreateTxRequest req) {
    return service.create(req)
        .map(tx -> ResponseEntity.status(HttpStatus.CREATED).body(tx));
  }

  @GetMapping("/transactions")
  public Flux<Transaction> list(@RequestParam String accountNumber) {
    return service.byAccount(accountNumber);
  }

  @GetMapping(value = "/stream/transactions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<Transaction>> stream() {
    return service.stream();
  }
}
