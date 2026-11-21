package com.project.cravely.models;

import com.project.cravely.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {

    private Long id;

    private Long orderId;

    private Long deliveryPartnerId;

    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    private LocalDateTime assignedAt;

    private LocalDateTime pickedUpAt;

    private LocalDateTime deliveredAt;
}

