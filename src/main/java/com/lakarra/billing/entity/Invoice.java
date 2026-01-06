package com.lakarra.billing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Invoice {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String cardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageType packageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddonType addonType;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;
    
    @Column(nullable = false)
    private boolean paid = false;
    
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Initializes default values and generates the invoice number before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (this.invoiceNumber == null) {
            this.invoiceNumber = "INV-" + this.cardId + "-" + System.currentTimeMillis();
        }
    }

}

