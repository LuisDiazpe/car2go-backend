package com.pe.platform.payment.domain.model.aggregates;

import com.pe.platform.payment.domain.model.valueobjects.PaymentStatus;
import com.pe.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * Transaction aggregate — Payment BC
 * US-16: Pago seguro entre comprador y vendedor
 * US-17: Confirmación de transacción al vendedor
 * US-18: Historial de transacciones
 */
@Getter
@Entity
public class Transaction extends AuditableAbstractAggregateRoot<Transaction> {

    @Column(nullable = false)
    private Long buyerProfileId;

    @Column(nullable = false)
    private Long sellerProfileId;

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 50)
    private String paymentMethod;

    @Column(length = 200)
    private String notes;

    protected Transaction() {}

    public Transaction(Long buyerProfileId, Long sellerProfileId, Long vehicleId,
                       Double amount, String paymentMethod) {
        this.buyerProfileId = buyerProfileId;
        this.sellerProfileId = sellerProfileId;
        this.vehicleId = vehicleId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }

    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in PENDING status");
        }
        this.status = PaymentStatus.COMPLETED;
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.notes = reason;
    }

    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot refund a non-completed transaction");
        }
        this.status = PaymentStatus.REFUNDED;
    }
}
