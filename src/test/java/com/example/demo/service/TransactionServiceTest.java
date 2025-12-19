package com.example.demo.service;

import com.example.demo.api.dto.CreateTxRequest;
import com.example.demo.api.error.BusinessException;
import com.example.demo.config.LogContext;
import com.example.demo.domain.mongo.Account;
import com.example.demo.domain.mongo.Transaction;
import com.example.demo.repository.mongo.AccountRepository;
import com.example.demo.repository.mongo.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;
  private RiskRemoteClient riskClient;
  private LogContext logContext;

  private TransactionService service;

  @BeforeEach
  void setup() {
    accountRepository = Mockito.mock(AccountRepository.class);
    transactionRepository = Mockito.mock(TransactionRepository.class);
    riskClient = Mockito.mock(RiskRemoteClient.class);

    // ✅ LogContext mock: devuelve el mismo mono sin cambiarlo
    logContext = Mockito.mock(LogContext.class);
    when(logContext.withMdc(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

    // ✅ constructor con LogContext
    service = new TransactionService(accountRepository, transactionRepository, riskClient, logContext);
  }

  @Test
  void debit_ok_should_create_transaction() {
    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("100"));

    Account acc = Account.builder()
        .id("acc1")
        .number("001-0001")
        .currency("PEN")
        .balance(new BigDecimal("2000"))
        .build();

    when(accountRepository.findByNumber("001-0001")).thenReturn(Mono.just(acc));
    when(riskClient.isAllowed("PEN", "DEBIT", new BigDecimal("100"))).thenReturn(Mono.just(true));

    when(accountRepository.save(any(Account.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
      Transaction t = inv.getArgument(0);
      t.setId("tx1");
      t.setTimestamp(Instant.now());
      return Mono.just(t);
    });

    StepVerifier.create(service.create(req))
        .expectNextMatches(tx -> tx.getId() != null && "DEBIT".equals(tx.getType()))
        .verifyComplete();
  }

  @Test
  void debit_insufficient_funds_should_fail() {
    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("5000"));

    Account acc = Account.builder()
        .id("acc1")
        .number("001-0001")
        .currency("PEN")
        .balance(new BigDecimal("2000"))
        .build();

    when(accountRepository.findByNumber("001-0001")).thenReturn(Mono.just(acc));
    when(riskClient.isAllowed("PEN", "DEBIT", new BigDecimal("5000"))).thenReturn(Mono.just(true));

    StepVerifier.create(service.create(req))
        .expectErrorMatches(ex ->
            ex instanceof BusinessException &&
            ((BusinessException) ex).getCode().equals("insufficient_funds"))
        .verify();
  }

  @Test
  void debit_risk_rejected_should_fail() {
    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("1600"));

    Account acc = Account.builder()
        .id("acc1")
        .number("001-0001")
        .currency("PEN")
        .balance(new BigDecimal("2000"))
        .build();

    when(accountRepository.findByNumber("001-0001")).thenReturn(Mono.just(acc));
    when(riskClient.isAllowed("PEN", "DEBIT", new BigDecimal("1600"))).thenReturn(Mono.just(false));

    StepVerifier.create(service.create(req))
        .expectErrorMatches(ex ->
            ex instanceof BusinessException &&
            ((BusinessException) ex).getCode().equals("risk_rejected"))
        .verify();
  }
}
