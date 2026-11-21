
package com.project.cravely.services.payment;

import java.math.BigDecimal;

public interface PaymentGateway {

    GatewayOrder createOrder(
            Long paymentId,
            Long orderId,
            BigDecimal amount,
            String currency
    );

    boolean verifyPaymentSignature(
            String gatewayOrderId,
            String gatewayPaymentId,
            String signature
    );

    GatewayPayment fetchPayment(String gatewayPaymentId);

    record GatewayOrder(
            String gatewayOrderId,
            Long amountInSmallestUnit,
            String currency,
            String status
    ) {
    }

    record GatewayPayment(
            String gatewayPaymentId,
            String gatewayOrderId,
            Long amountInSmallestUnit,
            String currency,
            String status,
            String method
    ) {
    }
}
