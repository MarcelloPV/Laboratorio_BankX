package com.example.demo.service;

import com.example.demo.domain.jpa.RiskRule;
import com.example.demo.repository.jpa.RiskRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RiskService {

  private final RiskRuleRepository riskRuleRepository;

  public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {

    // ✅ Si no es DEBIT, siempre permitido (y NO se consulta repo)
    if (!"DEBIT".equalsIgnoreCase(type)) {
      return Mono.just(true);
    }

    // ✅ Importante: NO devolver null en callable
    return Mono.fromCallable(() -> riskRuleRepository.findFirstByCurrency(currency))
        .subscribeOn(Schedulers.boundedElastic())
        .map(optRule -> {
          if (optRule.isEmpty()) return true; // sin regla -> permitido
          RiskRule rule = optRule.get();
          return amount.compareTo(rule.getMaxDebitPerTx()) <= 0;
        });
  }
}
