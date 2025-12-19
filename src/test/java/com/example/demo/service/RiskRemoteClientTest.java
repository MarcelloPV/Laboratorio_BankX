package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
        Mono.fromCompletionStage(client.fallbackTimed("PEN", "DEBIT", new BigDecimal("1600"), new RuntimeException("down")))
    ).expectNext(false)
     .verifyComplete();
  }
}
