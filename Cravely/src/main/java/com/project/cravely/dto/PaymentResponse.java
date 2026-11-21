package com.project.cravely.dto;

import com.project.cravely.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String gateway,
        String gatewayOrderId,
        String razorpayKeyId
) {
}