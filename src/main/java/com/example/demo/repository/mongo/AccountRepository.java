package com.example.demo.repository.mongo;

import com.example.demo.domain.mongo.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {
  Mono<Account> findByNumber(String number);
}