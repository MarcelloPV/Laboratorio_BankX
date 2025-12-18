package com.example.demo.api.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
  private String error;
  private String message;
}
