
package com.project.cravely.services.payment;

import com.project.cravely.enums.OrderStatus;
import com.project.cravely.enums.PaymentMethod;
import com.project.cravely.enums.PaymentStatus;
import com.project.cravely.models.Order;
import com.project.cravely.models.Payment;
import com.project.cravely.repositories.OrderRepository;
import com.project.cravely.repositories.PaymentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentPersistenceService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPendingPayment(
            Long orderId,
            String idempotencyKey
    ) {

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + orderId
                        )
                );

        Payment existingPayment = paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existingPayment != null) {

            if (!existingPayment.getOrderId().equals(orderId)) {
                throw new IllegalArgumentException(
                        "Idempotency key has already been used for another order"
                );
            }

            if (existingPayment.getGatewayOrderId() == null
                    && existingPayment.getStatus() == PaymentStatus.PENDING) {
                throw new IllegalStateException(
                        "Payment creation is already in progress or requires reconciliation"
                );
            }

            return existingPayment;
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException(
                    "Payment can only be initiated for CREATED orders"
            );
        }

        if (paymentRepository.existsByOrderIdAndStatus(
                orderId, PaymentStatus.PENDING)) {
            throw new IllegalStateException(
                    "A payment attempt is already pending for this order"
            );
        }

        if (paymentRepository.existsByOrderIdAndStatus(
                orderId, PaymentStatus.SUCCESS)) {
            throw new IllegalStateException(
                    "This order has already been paid"
            );
        }

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .gateway("RAZORPAY")
                .idempotencyKey(idempotencyKey)
                .build();

        return paymentRepository.saveAndFlush(payment);
    }

    @Transactional
    public Payment attachGatewayOrder(
            Long paymentId,
            String gatewayOrderId
    ) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: " + paymentId
                        )
                );

        payment.attachGatewayOrder(gatewayOrderId);

        return paymentRepository.saveAndFlush(payment);
    }

    @Transactional
    public void markPaymentFailed(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: " + paymentId
                        )
                );

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.markFailed();
            paymentRepository.saveAndFlush(payment);
        }
    }

    @Transactional
    public Payment verifyAndCompletePayment(
            Long paymentId,
            String gatewayOrderId,
            String gatewayPaymentId,
            PaymentMethod paymentMethod
    ) {

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: " + paymentId
                        )
                );

        // Handle duplicate verification requests.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            if (gatewayOrderId.equals(payment.getGatewayOrderId())
                    && gatewayPaymentId.equals(payment.getGatewayPaymentId())) {
                return payment;
            }

            throw new IllegalStateException(
                    "Payment has already been completed with different gateway details"
            );
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment cannot be completed from status: "
                            + payment.getStatus()
            );
        }

        if (!gatewayOrderId.equals(payment.getGatewayOrderId())) {
            throw new IllegalArgumentException(
                    "Razorpay order ID does not match the payment record"
            );
        }

        if (gatewayPaymentId == null || gatewayPaymentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Razorpay payment ID is required"
            );
        }

        payment.markSuccess(gatewayPaymentId, paymentMethod);

        return paymentRepository.saveAndFlush(payment);
    }

    @Transactional(readOnly = true)
    public Payment findByGatewayOrderId(String gatewayOrderId) {

        return paymentRepository.findByGatewayOrderId(gatewayOrderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found for Razorpay order: " + gatewayOrderId
                        )
                );
    }
}
