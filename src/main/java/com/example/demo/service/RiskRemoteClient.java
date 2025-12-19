package com.example.demo.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
@RequiredArgsConstructor
public class RiskRemoteClient {

  private final WebClient riskWebClient;
  private final RiskService legacyRisk;

  public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
    return Mono.fromCompletionStage(isAllowedTimed(currency, type, amount));
  }

  @TimeLimiter(name = "riskClient", fallbackMethod = "fallbackTimed")
  @Retry(name = "riskClient")
  @CircuitBreaker(name = "riskClient", fallbackMethod = "fallbackTimed")
  public CompletionStage<Boolean> isAllowedTimed(String currency, String type, BigDecimal amount) {
    return callRemote(currency, type, amount)
        .toFuture(); 
  }

  private Mono<Boolean> callRemote(String currency, String type, BigDecimal amount) {
    return riskWebClient.get()
        .uri(uri -> uri.path("/allow")
            .queryParam("currency", currency)
            .queryParam("type", type)
            .queryParam("amount", amount)
            .queryParam("fail", false)
            .queryParam("delayMs", 200) 
            .build())
        .retrieve()
        .bodyToMono(Boolean.class);
  }

  public CompletionStage<Boolean> fallbackTimed(String currency, String type, BigDecimal amount, Throwable ex) {
    return legacyRisk.isAllowed(currency, type, amount)
        .toFuture();
  }
}
