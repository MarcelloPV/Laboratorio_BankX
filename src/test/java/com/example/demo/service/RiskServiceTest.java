package com.example.demo.service;

import com.example.demo.repository.jpa.RiskRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;

class RiskServiceTest {

  private RiskRuleRepository repo;
  private RiskService service;

  @BeforeEach
  void setup() {
    repo = Mockito.mock(RiskRuleRepository.class);
    service = new RiskService(repo);
  }

  @Test
  void credit_should_always_be_allowed() {
    StepVerifier.create(service.isAllowed("PEN", "CREDIT", new BigDecimal("9999")))
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void debit_when_no_rule_should_be_allowed() {
    when(repo.findFirstByCurrency("PEN")).thenReturn(Optional.empty());

    StepVerifier.create(service.isAllowed("PEN", "DEBIT", new BigDecimal("2000")))
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void debit_when_rule_exists_and_amount_ok_should_be_allowed() {
    var rule = new com.example.demo.domain.jpa.RiskRule();
    rule.setCurrency("PEN");
    rule.setMaxDebitPerTx(new BigDecimal("1500"));

    when(repo.findFirstByCurrency("PEN")).thenReturn(Optional.of(rule));

    StepVerifier.create(service.isAllowed("PEN", "DEBIT", new BigDecimal("1000")))
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void debit_when_rule_exists_and_amount_too_high_should_be_rejected() {
    var rule = new com.example.demo.domain.jpa.RiskRule();
    rule.setCurrency("PEN");
    rule.setMaxDebitPerTx(new BigDecimal("1500"));

    when(repo.findFirstByCurrency("PEN")).thenReturn(Optional.of(rule));

    StepVerifier.create(service.isAllowed("PEN", "DEBIT", new BigDecimal("1600")))
        .expectNext(false)
        .verifyComplete();
  }
}
