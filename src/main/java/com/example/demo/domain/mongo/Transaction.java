package com.example.demo.domain.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;


@Document("transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {
    @Id
    private String id;

    private String accountId;
    private String type;
    private BigDecimal amount;
    private Instant timestamp;
    private String status;
    private String reason;
}
