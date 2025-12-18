package com.example.demo.repository.jpa;

import com.example.demo.domain.jpa.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskRuleRepository extends JpaRepository<RiskRule, Long> {
  Optional<RiskRule> findFirstByCurrency(String currency);
}
