package com.project.cravely.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        Long restaurantId,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
