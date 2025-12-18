package com.example.demo.service;

import com.example.demo.repository.jpa.RiskRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RiskService {

  private final RiskRuleRepository riskRuleRepository;

  public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
    return Mono.fromCallable(() -> riskRuleRepository.findFirstByCurrency(currency).orElse(null))
        .subscribeOn(Schedulers.boundedElastic())
        .map(rule -> {
          if (!"DEBIT".equalsIgnoreCase(type)) return true;
          if (rule == null) return true;
          return amount.compareTo(rule.getMaxDebitPerTx()) <= 0;
        });
  }
}
