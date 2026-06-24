package com.hostelapp.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private Instant paymentDate = Instant.now();

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // UPI, CARD, CASH, NET_BANKING

    @Column(name = "transaction_reference", nullable = false, unique = true)
    private String transactionReference;
}
