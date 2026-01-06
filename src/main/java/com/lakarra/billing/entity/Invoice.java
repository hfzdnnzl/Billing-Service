package com.lakarra.billing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Invoice {
    @Id
    @GeneratedValue
    private Long id;
    private String userId;
    private String cardId;
    @Enumerated(EnumType.STRING)
    private PackageType packageType;
    @Enumerated(EnumType.STRING)
    private AddonType addonType;
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private boolean paid;
    private LocalDateTime createdAt;

    // Constructors
    public Invoice() {}

    public Invoice(
        String userId, String cardId, PackageType packageType, 
        AddonType addonType, BigDecimal amount, String currency) {

        this.userId = userId;
        this.cardId = cardId;
        this.packageType = packageType;
        this.addonType = addonType;
        this.amount = amount;
        this.currency = currency;

        this.invoiceNumber = "INV-" + System.currentTimeMillis();
        this.paid = false;
        this.createdAt = LocalDateTime.now();
    }


}
