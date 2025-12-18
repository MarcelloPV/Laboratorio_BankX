package com.example.demo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTxRequest {
  @NotBlank
  private String accountNumber;

  @NotBlank
  private String type;

  @NotNull
  @Positive
  private BigDecimal amount;
}
