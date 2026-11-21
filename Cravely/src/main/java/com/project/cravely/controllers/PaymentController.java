
package com.project.cravely.controllers;

import com.project.cravely.dto.PaymentResponse;
import com.project.cravely.dto.PaymentVerificationRequest;
import com.project.cravely.dto.PaymentVerificationResponse;
import com.project.cravely.services.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a Razorpay payment order for an existing Cravely order.
     */
    @PostMapping("/orders/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @PathVariable Long orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return paymentService.createPayment(orderId, idempotencyKey);
    }

    /**
     * Verify the payment after Razorpay Checkout completes.
     */
    @PostMapping("/verify")
    public PaymentVerificationResponse verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request
    ) {
        return paymentService.verifyPayment(request);
    }
}
