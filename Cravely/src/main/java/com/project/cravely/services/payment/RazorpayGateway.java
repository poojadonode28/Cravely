
package com.project.cravely.services.payment;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class RazorpayGateway implements PaymentGateway {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Override
    public GatewayOrder createOrder(
            Long paymentId,
            Long orderId,
            BigDecimal amount,
            String currency
    ) {
        try {
            validateRequest(paymentId, orderId, amount, currency);

            long amountInPaise = amount
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            JSONObject request = new JSONObject();
            request.put("amount", amountInPaise);
            request.put("currency", currency);
            request.put("receipt", "cravely_" + paymentId);

            JSONObject notes = new JSONObject();
            notes.put("payment_id", paymentId.toString());
            notes.put("cravely_order_id", orderId.toString());

            request.put("notes", notes);

            log.info(
                    "Creating Razorpay order. PaymentId={}, OrderId={}, Amount={}",
                    paymentId,
                    orderId,
                    amount
            );

            Order razorpayOrder = razorpayClient.orders.create(request);

            return new GatewayOrder(
                    razorpayOrder.get("id"),
                    ((Number) razorpayOrder.get("amount")).longValue(),
                    razorpayOrder.get("currency"),
                    razorpayOrder.get("status")
            );

        } catch (RazorpayException ex) {
            log.error(
                    "Razorpay order creation failed. PaymentId={}, Error={}",
                    paymentId,
                    ex.getMessage(),
                    ex
            );

            throw new IllegalStateException(
                    "Unable to create Razorpay order",
                    ex
            );
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Invalid payment amount",
                    ex
            );
        }
    }

    @Override
    public boolean verifyPaymentSignature(
            String gatewayOrderId,
            String gatewayPaymentId,
            String signature
    ) {
        if (gatewayOrderId == null || gatewayPaymentId == null
                || signature == null || signature.isBlank()) {
            return false;
        }

        try {
            String payload = gatewayOrderId + "|" + gatewayPaymentId;

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] generatedSignature = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            byte[] receivedSignature = HexFormat.of().parseHex(signature);

            return MessageDigest.isEqual(
                    generatedSignature,
                    receivedSignature
            );

        } catch (IllegalArgumentException ex) {
            // Invalid hexadecimal signature.
            return false;

        } catch (Exception ex) {
            log.error("Razorpay signature verification error", ex);

            throw new IllegalStateException(
                    "Unable to verify Razorpay payment signature",
                    ex
            );
        }
    }

    private void validateRequest(
            Long paymentId,
            Long orderId,
            BigDecimal amount,
            String currency
    ) {
        if (paymentId == null || orderId == null) {
            throw new IllegalArgumentException(
                    "Payment ID and Order ID are required"
            );
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (!"INR".equals(currency)) {
            throw new IllegalArgumentException(
                    "Unsupported payment currency: " + currency
            );
        }

        amount.multiply(BigDecimal.valueOf(100)).longValueExact();
    }
    @Override
    public GatewayPayment fetchPayment(String gatewayPaymentId) {
        try {
            com.razorpay.Payment razorpayPayment =
                    razorpayClient.payments.fetch(gatewayPaymentId);

            return new GatewayPayment(
                    razorpayPayment.get("id"),
                    razorpayPayment.get("order_id"),
                    ((Number) razorpayPayment.get("amount")).longValue(),
                    razorpayPayment.get("currency"),
                    razorpayPayment.get("status"),
                    razorpayPayment.get("method")
            );

        } catch (RazorpayException ex) {
            log.error(
                    "Unable to fetch Razorpay payment. PaymentId={}, Error={}",
                    gatewayPaymentId,
                    ex.getMessage(),
                    ex
            );

            throw new IllegalStateException(
                    "Unable to fetch Razorpay payment details",
                    ex
            );
        }
    }
}
