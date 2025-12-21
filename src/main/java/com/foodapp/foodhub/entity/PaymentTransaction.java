package com.foodapp.foodhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments_transactions")
@Getter
@Setter @NoArgsConstructor
public class PaymentTransaction extends BaseEntity {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id_fk")
    private Order order;

    private String paymobOrderId;
    private Long amountCents;
    private boolean success;
    private String currency;

    @Column(name = "txn_status")
    private String status;

    @Column(columnDefinition = "TEXT")
    private String rawResponse; // Useful for debugging future issues
}
