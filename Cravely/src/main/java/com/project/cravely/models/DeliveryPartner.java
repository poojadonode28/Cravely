
package com.project.cravely.models;

import com.project.cravely.enums.DeliveryPartnerStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "delivery_partners",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_delivery_partners_phone",
                        columnNames = "phone"
                )
        },
        indexes = {
                @Index(
                        name = "idx_delivery_partners_status",
                        columnList = "status"
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DeliveryPartnerStatus status =
            DeliveryPartnerStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void markOnDelivery() {
        if (status != DeliveryPartnerStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Delivery partner is not available"
            );
        }

        this.status = DeliveryPartnerStatus.ON_DELIVERY;
    }

    public void markAvailable() {
        if (status != DeliveryPartnerStatus.ON_DELIVERY) {
            throw new IllegalStateException(
                    "Delivery partner is not currently on delivery"
            );
        }

        this.status = DeliveryPartnerStatus.AVAILABLE;
    }

    public void deactivate() {
        if (status == DeliveryPartnerStatus.ON_DELIVERY) {
            throw new IllegalStateException(
                    "Cannot deactivate a partner during an active delivery"
            );
        }

        this.status = DeliveryPartnerStatus.INACTIVE;
    }

    public void activate() {
        if (status != DeliveryPartnerStatus.INACTIVE) {
            throw new IllegalStateException(
                    "Only inactive partners can be activated"
            );
        }

        this.status = DeliveryPartnerStatus.AVAILABLE;
    }
}
