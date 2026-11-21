
package com.project.cravely.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RazorpayConfig {

    @Bean
    public RazorpayClient razorpayClient(
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret
    ) throws RazorpayException {

        String cleanKeyId = keyId == null ? "" : keyId.trim();
        String cleanKeySecret = keySecret == null ? "" : keySecret.trim();

        if (cleanKeyId.isBlank() || cleanKeySecret.isBlank()) {
            throw new IllegalStateException(
                    "Razorpay API credentials are missing"
            );
        }

        if (!cleanKeyId.startsWith("rzp_test_")
                && !cleanKeyId.startsWith("rzp_live_")) {
            throw new IllegalStateException(
                    "Invalid Razorpay Key ID format"
            );
        }

        log.info("Initializing Razorpay client");
        log.info("Razorpay Key ID prefix: {}",
                cleanKeyId.substring(0, Math.min(8, cleanKeyId.length())));
        log.info("Razorpay Key ID length: {}", cleanKeyId.length());
        log.info("Razorpay Secret length: {}", cleanKeySecret.length());
        log.info("Razorpay Key ID has surrounding whitespace: {}",
                !keyId.equals(cleanKeyId));
        log.info("Razorpay Secret has surrounding whitespace: {}",
                !keySecret.equals(cleanKeySecret));

        return new RazorpayClient(cleanKeyId, cleanKeySecret);
    }
}
