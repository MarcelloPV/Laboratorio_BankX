package com.example.demo.api.controller;

import com.example.demo.api.dto.CreateTxRequest;
import com.example.demo.domain.mongo.Account;
import com.example.demo.domain.mongo.Transaction;
import com.example.demo.repository.mongo.AccountRepository;
import com.example.demo.repository.mongo.TransactionRepository;
import com.example.demo.service.RiskRemoteClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TransactionControllerTest {

  @MockBean AccountRepository accountRepository;
  @MockBean TransactionRepository transactionRepository;
  @MockBean RiskRemoteClient riskRemoteClient;

  /*private final WebTestClient client;

  TransactionControllerTest(WebTestClient client) {
    this.client = client;
  }*/
  @Autowired
  private WebTestClient client;


  private Account acc;

  @BeforeEach
  void setup() {
    acc = Account.builder()
        .id("acc1")
        .number("001-0001")
        .currency("PEN")
        .balance(new BigDecimal("2000"))
        .build();
  }

  @Test
  void post_and_get_transaction_ok() {
    when(accountRepository.findByNumber("001-0001")).thenReturn(Mono.just(acc));
    when(riskRemoteClient.isAllowed("PEN", "DEBIT", new BigDecimal("100"))).thenReturn(Mono.just(true));
    when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(acc));

    when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
      Transaction t = inv.getArgument(0);
      t.setId("tx1");
      t.setTimestamp(Instant.now());
      return Mono.just(t);
    });

    when(transactionRepository.findByAccountIdOrderByTimestampDesc("acc1"))
    .thenReturn(Flux.just(
        Transaction.builder()
            .id("tx1")
            .accountId("acc1")
            .type("DEBIT")
            .amount(new BigDecimal("100"))
            .status("OK")
            .timestamp(Instant.now())
            .build()
    ));

    // POST
    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("100"));

    client.post()
        .uri("/api/transactions")
        .header("X-Correlation-Id", "test-it")
        .bodyValue(req)
        .exchange()
        .expectStatus().is2xxSuccessful()
        .expectBody()
        .jsonPath("$.id").isEqualTo("tx1");

    // GET
    client.get()
        .uri("/api/transactions?accountNumber=001-0001")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].id").isEqualTo("tx1");
  }
}
