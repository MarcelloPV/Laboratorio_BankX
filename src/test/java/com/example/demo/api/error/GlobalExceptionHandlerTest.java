package com.example.demo.api.error;

import com.example.demo.api.dto.CreateTxRequest;
import com.example.demo.repository.mongo.AccountRepository;
import com.example.demo.service.RiskRemoteClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GlobalExceptionHandlerTest {

  @MockBean AccountRepository accountRepository;
  @MockBean RiskRemoteClient riskRemoteClient;

  /*private final WebTestClient client;

  GlobalExceptionHandlerTest(WebTestClient client) {
    this.client = client;
  }*/
  @Autowired
  private WebTestClient client;



  @Test
  void account_not_found_should_return_404() {
    when(accountRepository.findByNumber("XXX")).thenReturn(Mono.empty());

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("XXX");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("100"));

    client.post()
        .uri("/api/transactions")
        .bodyValue(req)
        .exchange()
        .expectStatus().is4xxClientError()
        .expectBody()
        .jsonPath("$.error").exists()
        .jsonPath("$.message").exists();
  }
}
