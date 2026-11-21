package com.project.cravely.models;

import com.project.cravely.enums.PaymentMethod;
import com.project.cravely.enums.PaymentStatus;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_order_id", columnList = "order_id"),
                @Index(name = "idx_payments_status", columnList = "status")
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String gateway = "RAZORPAY";

    @Column(name = "gateway_order_id", unique = true, length = 100)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", unique = true, length = 100)
    private String gatewayPaymentId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markSuccess(String gatewayPaymentId, PaymentMethod method) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment cannot be completed from status: " + status
            );
        }

        if (gatewayPaymentId == null || gatewayPaymentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Gateway payment ID is required"
            );
        }

        this.gatewayPaymentId = gatewayPaymentId;
        this.paymentMethod = method;
        this.status = PaymentStatus.SUCCESS;
    }

    public void markFailed() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment cannot be failed from status: " + status
            );
        }

        this.status = PaymentStatus.FAILED;
    }

    public void markRefunded() {
        if (status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Only successful payments can be refunded"
            );
        }

        this.status = PaymentStatus.REFUNDED;
    }

    public void attachGatewayOrder(String gatewayOrderId) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Gateway order can only be attached to a pending payment"
            );
        }

        if (gatewayOrderId == null || gatewayOrderId.isBlank()) {
            throw new IllegalArgumentException(
                    "Gateway order ID is required"
            );
        }

        this.gatewayOrderId = gatewayOrderId;
    }

}