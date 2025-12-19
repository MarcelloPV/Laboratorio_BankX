package com.example.demo.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MockRiskControllerTest {

  /*private final WebTestClient client;

  MockRiskControllerTest(WebTestClient client) {
    this.client = client;
  }*/
  @Autowired
  private WebTestClient client;

  @Test
  void allow_should_return_boolean() {
    client.get()
        .uri(uri -> uri.path("/api/mock/risk/allow")
            .queryParam("currency", "PEN")
            .queryParam("type", "DEBIT")
            .queryParam("amount", "100")
            .queryParam("fail", "false")
            .queryParam("delayMs", "0")
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(true);
  }

  @Test
  void fail_should_return_5xx_or_error() {
    client.get()
        .uri(uri -> uri.path("/api/mock/risk/allow")
            .queryParam("currency", "PEN")
            .queryParam("type", "DEBIT")
            .queryParam("amount", "100")
            .queryParam("fail", "true")
            .queryParam("delayMs", "0")
            .build())
        .exchange()
        .expectStatus().is5xxServerError();
  }
}
