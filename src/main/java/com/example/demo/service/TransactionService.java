package com.example.demo.service;

import com.example.demo.api.dto.CreateTxRequest;
import com.example.demo.api.error.BusinessException;
import com.example.demo.domain.mongo.Account;
import com.example.demo.domain.mongo.Transaction;
import com.example.demo.repository.mongo.AccountRepository;
import com.example.demo.repository.mongo.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final RiskService riskService;

  private final Sinks.Many<Transaction> txSink =
      Sinks.many().multicast().onBackpressureBuffer();

  public Mono<Transaction> create(CreateTxRequest req) {
    String type = req.getType().toUpperCase();

    return accountRepository.findByNumber(req.getAccountNumber())
        .switchIfEmpty(Mono.error(new BusinessException(
            "account_not_found",
            "No existe la cuenta: " + req.getAccountNumber()
        )))
        .flatMap(account ->
            riskService.isAllowed(account.getCurrency(), type, req.getAmount())
                .flatMap(allowed -> {
                  if (!allowed) {
                    return Mono.error(new BusinessException(
                        "risk_rejected",
                        "Transacción rechazada por regla de riesgo"
                    ));
                  }
                  return applyAndSave(account, type, req.getAmount());
                })
        );
  }

  private Mono<Transaction> applyAndSave(Account account, String type, BigDecimal amount) {
    if ("DEBIT".equals(type)) {
      if (account.getBalance().compareTo(amount) < 0) {
        return Mono.error(new BusinessException(
            "insufficient_funds",
            "Fondos insuficientes. Saldo: " + account.getBalance()
        ));
      }
      account.setBalance(account.getBalance().subtract(amount));
    } else if ("CREDIT".equals(type)) {
      account.setBalance(account.getBalance().add(amount));
    } else {
      return Mono.error(new BusinessException(
          "invalid_type",
          "type debe ser DEBIT o CREDIT"
      ));
    }

    Transaction tx = Transaction.builder()
        .accountId(account.getId())
        .type(type)
        .amount(amount)
        .timestamp(Instant.now())
        .status("OK")
        .reason(null)
        .build();

    return accountRepository.save(account)
        .publishOn(Schedulers.parallel())
        .then(transactionRepository.save(tx))
        .doOnNext(saved -> txSink.tryEmitNext(saved));
  }

  public Flux<Transaction> byAccount(String accountNumber) {
    return accountRepository.findByNumber(accountNumber)
        .switchIfEmpty(Mono.error(new BusinessException(
            "account_not_found",
            "No existe la cuenta: " + accountNumber
        )))
        .flatMapMany(acc -> transactionRepository.findByAccountIdOrderByTimestampDesc(acc.getId()));
  }

  public Flux<ServerSentEvent<Transaction>> stream() {
    return txSink.asFlux()
        .map(tx -> ServerSentEvent.builder(tx).event("transaction").build());
  }
}
