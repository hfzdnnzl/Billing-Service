package com.lakarra.billing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment {
    @Id @GeneratedValue
    private Long id;
    private BigDecimal amount;
    private String method; // e.g., TOYYIBPAY, CREDIT_CARD
    private Long invoiceId;
    private LocalDateTime paidAt;
}
