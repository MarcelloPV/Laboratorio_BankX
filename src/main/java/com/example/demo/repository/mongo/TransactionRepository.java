package com.example.demo.repository.mongo;

import com.example.demo.domain.mongo.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
  Flux<Transaction> findByAccountIdOrderByTimestampDesc(String accountId);
}
