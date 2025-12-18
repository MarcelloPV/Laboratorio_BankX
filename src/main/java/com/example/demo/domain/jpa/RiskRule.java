package com.example.demo.domain.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "risk_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RiskRule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String currency;
  private BigDecimal maxDebitPerTx;
}
