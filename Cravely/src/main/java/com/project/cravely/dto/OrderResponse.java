package com.project.cravely.dto;

import com.project.cravely.enums.OrderStatus;
import com.project.cravely.models.Order;
import com.project.cravely.models.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private Long restaurantId;
    private Long deliveryPartnerId;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private Long version;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .restaurantId(order.getRestaurantId())
                .deliveryPartnerId(order.getDeliveryPartnerId())
                .items(order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .version(order.getVersion())
                .build();
    }

    @Getter
    @Builder
    public static class OrderItemResponse {
        private Long menuItemId;
        private String name;
        private BigDecimal price;
        private int quantity;

        public static OrderItemResponse from(OrderItem item) {
            return OrderItemResponse.builder()
                    .menuItemId(item.getMenuItemId())
                    .name(item.getName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .build();
        }
    }
}