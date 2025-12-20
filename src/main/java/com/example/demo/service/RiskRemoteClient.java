package com.example.demo.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RiskRemoteClient {

  private final WebClient riskWebClient;
  private final RiskService legacyRisk;

  @TimeLimiter(name = "riskClient")
  @Retry(name = "riskClient")
  @CircuitBreaker(name = "riskClient", fallbackMethod = "fallback")
  public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
    return riskWebClient.get()
        .uri(uri -> uri.path("/allow")
            .queryParam("currency", currency)
            .queryParam("type", type)
            .queryParam("amount", amount)
            .queryParam("fail", false)
            .queryParam("delayMs", 200) // simula latencia
            .build())
        .retrieve()
        .bodyToMono(Boolean.class);
  }

  public Mono<Boolean> fallback(String currency, String type, BigDecimal amount, Throwable ex) {
    return legacyRisk.isAllowed(currency, type, amount);

  }

  @Autowired
  RiskService legacy; // el de JPA que ya tenías

  private Mono<Boolean> legacyAllowed(String c, String t, BigDecimal a) {
    return legacy.isAllowed(c, t, a); // ya corre en boundedElastic
  }
}
