
package com.project.cravely.models;

import com.project.cravely.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user_id", columnList = "user_id"),
                @Index(name = "idx_orders_restaurant_id", columnList = "restaurant_id")
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "delivery_partner_id")
    private Long deliveryPartnerId;

    @ElementCollection
    @CollectionTable(
            name = "order_items",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @OrderColumn(name = "item_position")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

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

    public void acceptByRestaurant() {
        validateStatus(OrderStatus.CREATED);
        this.status = OrderStatus.ACCEPTED;
    }

    public void rejectByRestaurant() {
        validateStatus(OrderStatus.CREATED);
        this.status = OrderStatus.REJECTED;
    }

    public void startPreparing() {
        validateStatus(OrderStatus.ACCEPTED);
        this.status = OrderStatus.PREPARING;
    }

    public void markReadyForPickup() {
        validateStatus(OrderStatus.PREPARING);
        this.status = OrderStatus.READY_FOR_PICKUP;
    }

    public void markOutForDelivery(Long deliveryPartnerId) {
        validateStatus(OrderStatus.READY_FOR_PICKUP);

        if (deliveryPartnerId == null) {
            throw new IllegalArgumentException(
                    "Delivery partner ID is required"
            );
        }

        this.deliveryPartnerId = deliveryPartnerId;
        this.status = OrderStatus.OUT_FOR_DELIVERY;
    }

    public void markDelivered() {
        validateStatus(OrderStatus.OUT_FOR_DELIVERY);
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED
                || status == OrderStatus.REJECTED
                || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Order cannot be cancelled in status: " + status
            );
        }

        this.status = OrderStatus.CANCELLED;
    }

    private void validateStatus(OrderStatus expectedStatus) {
        if (this.status != expectedStatus) {
            throw new IllegalStateException(
                    "Invalid order transition. Expected: "
                            + expectedStatus + ", Current: " + status
            );
        }
    }
}
