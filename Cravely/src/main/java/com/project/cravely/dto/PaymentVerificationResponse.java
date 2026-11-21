
package com.project.cravely.dto;

import com.project.cravely.enums.PaymentMethod;
import com.project.cravely.enums.PaymentStatus;

public record PaymentVerificationResponse(
        Long paymentId,
        Long orderId,
        String gatewayOrderId,
        String gatewayPaymentId,
        PaymentStatus status,
        PaymentMethod paymentMethod
) {
}

