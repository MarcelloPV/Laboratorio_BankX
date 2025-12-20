package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

class RiskRemoteClientTest {

  @Test
  void fallbackTimed_should_delegate_to_legacyRisk() {
    var legacy = Mockito.mock(RiskService.class);
    var client = new RiskRemoteClient(null, legacy); // WebClient no se usa aquí

    when(legacy.isAllowed("PEN", "DEBIT", new BigDecimal("1600")))
        .thenReturn(Mono.just(false));

    StepVerifier.create(
        client.fallback("PEN", "DEBIT", new BigDecimal("1600"), new RuntimeException("down"))).expectNext(false)
        .verifyComplete();
  }

  @Test
  void isAllowed_should_return_true() {
/*
    WebClient webClient = WebClient.builder()
        .baseUrl("http://localhost:8080")
        .build();
*/
    var legacy = Mockito.mock(RiskService.class);
    var client = new RiskRemoteClient(null, legacy);

    when(legacy.isAllowed("001-0001", "DEBIT", new BigDecimal("100.00")))
        .thenReturn(Mono.just(true));

    StepVerifier.create(
        client.isAllowed("PEN", "DEBIT", new BigDecimal("1600"))).expectNext(false)
        .verifyComplete();

  }
}
