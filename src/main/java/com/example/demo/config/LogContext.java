package com.example.demo.config;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LogContext {

  public <T> Mono<T> withMdc(Mono<T> mono) {
    return Mono.deferContextual(ctx -> {
      String corrId = ctx.getOrDefault("corrId", "na").toString();
      ThreadContext.put("corrId", corrId);
      return mono.doFinally(sig -> ThreadContext.remove("corrId"));
    });
  }
}
