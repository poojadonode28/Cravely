
package com.project.cravely.services.payment;

import com.project.cravely.dto.PaymentResponse;
import com.project.cravely.dto.PaymentVerificationRequest;
import com.project.cravely.dto.PaymentVerificationResponse;
import com.project.cravely.enums.PaymentMethod;
import com.project.cravely.enums.PaymentStatus;
import com.project.cravely.models.Payment;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentPersistenceService persistenceService;
    private final PaymentGateway paymentGateway;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    public PaymentResponse createPayment(
            Long orderId,
            String idempotencyKey
    ) {
        validateIdempotencyKey(idempotencyKey);

        Payment payment = persistenceService.createPendingPayment(
                orderId,
                idempotencyKey
        );

        if (payment.getGatewayOrderId() != null) {
            return toResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment cannot be initiated from status: "
                            + payment.getStatus()
            );
        }

        PaymentGateway.GatewayOrder gatewayOrder;

        try {
            gatewayOrder = paymentGateway.createOrder(
                    payment.getId(),
                    orderId,
                    payment.getAmount(),
                    payment.getCurrency()
            );
        } catch (RuntimeException ex) {
            persistenceService.markPaymentFailed(payment.getId());
            throw ex;
        }

        Payment updatedPayment = persistenceService.attachGatewayOrder(
                payment.getId(),
                gatewayOrder.gatewayOrderId()
        );

        return toResponse(updatedPayment);
    }

    public PaymentVerificationResponse verifyPayment(
            PaymentVerificationRequest request
    ) {

        // Find the Cravely payment using the Razorpay order ID.
        Payment payment = persistenceService.findByGatewayOrderId(
                request.razorpayOrderId()
        );

        // Handle duplicate verification requests.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (request.razorpayPaymentId().equals(
                    payment.getGatewayPaymentId())) {
                return toVerificationResponse(payment);
            }

            throw new IllegalStateException(
                    "Payment has already been completed with different payment details"
            );
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment cannot be verified from status: "
                            + payment.getStatus()
            );
        }

        // Verify Razorpay HMAC signature.
        boolean validSignature = paymentGateway.verifyPaymentSignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!validSignature) {
            throw new IllegalArgumentException(
                    "Invalid Razorpay payment signature"
            );
        }

        // Fetch authoritative payment details from Razorpay.
        PaymentGateway.GatewayPayment gatewayPayment =
                paymentGateway.fetchPayment(
                        request.razorpayPaymentId()
                );

        // Verify gateway order association.
        if (!request.razorpayOrderId().equals(
                gatewayPayment.gatewayOrderId())) {
            throw new IllegalArgumentException(
                    "Razorpay payment does not belong to this order"
            );
        }

        // Verify amount.
        long expectedAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        if (expectedAmount != gatewayPayment.amountInSmallestUnit()) {
            throw new IllegalArgumentException(
                    "Razorpay payment amount does not match Cravely order amount"
            );
        }

        // Verify currency.
        if (!payment.getCurrency().equals(gatewayPayment.currency())) {
            throw new IllegalArgumentException(
                    "Razorpay payment currency does not match"
            );
        }

        // Only captured payments can be marked successful.
        if (!"captured".equalsIgnoreCase(gatewayPayment.status())) {
            throw new IllegalStateException(
                    "Razorpay payment is not captured. Current status: "
                            + gatewayPayment.status()
            );
        }

        PaymentMethod method = mapPaymentMethod(
                gatewayPayment.method()
        );

        Payment completedPayment =
                persistenceService.verifyAndCompletePayment(
                        payment.getId(),
                        request.razorpayOrderId(),
                        request.razorpayPaymentId(),
                        method
                );

        return toVerificationResponse(completedPayment);
    }

    private PaymentMethod mapPaymentMethod(String method) {

        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException(
                    "Razorpay payment method is missing"
            );
        }

        return switch (method.toLowerCase(Locale.ROOT)) {
            case "upi" -> PaymentMethod.UPI;
            case "card" -> PaymentMethod.CARD;
            case "netbanking" -> PaymentMethod.NET_BANKING;
            case "wallet" -> PaymentMethod.WALLET;
            default -> throw new IllegalArgumentException(
                    "Unsupported payment method: " + method
            );
        };
    }

    private void validateIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        if (idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency-Key cannot exceed 100 characters"
            );
        }
    }

    private PaymentResponse toResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getGateway(),
                payment.getGatewayOrderId(),
                razorpayKeyId.trim()
        );
    }

    private PaymentVerificationResponse toVerificationResponse(
            Payment payment
    ) {
        return new PaymentVerificationResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getGatewayOrderId(),
                payment.getGatewayPaymentId(),
                payment.getStatus(),
                payment.getPaymentMethod()
        );
    }
}
