package com.example.demo.config;

import com.example.demo.domain.jpa.RiskRule;
import com.example.demo.domain.mongo.Account;
import com.example.demo.repository.jpa.RiskRuleRepository;
import com.example.demo.repository.mongo.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final RiskRuleRepository riskRuleRepository;
  private final AccountRepository accountRepository;

  @Override
  public void run(String... args) {
    riskRuleRepository.deleteAll();
    riskRuleRepository.save(RiskRule.builder().currency("PEN").maxDebitPerTx(new BigDecimal("1500")).build());
    riskRuleRepository.save(RiskRule.builder().currency("USD").maxDebitPerTx(new BigDecimal("500")).build());

    var accounts = List.of(
        Account.builder().number("001-0001").holderName("Juan Perez").currency("PEN").balance(new BigDecimal("2000")).build(),
        Account.builder().number("001-0002").holderName("Maria Lopez").currency("USD").balance(new BigDecimal("800")).build()
    );

    accountRepository.deleteAll()
        .thenMany(accountRepository.saveAll(accounts))
        .subscribe();
  }
}
