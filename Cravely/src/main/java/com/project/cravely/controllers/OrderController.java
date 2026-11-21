
package com.project.cravely.controllers;

import com.project.cravely.dto.OrderResponse;
import com.project.cravely.services.CurrentUserService;
import com.project.cravely.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    /**
     * Place an order for the authenticated customer.
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse placeOrder() {
        Long userId = currentUserService.getCurrentUserId();
        return orderService.placeOrder(userId);
    }

    /**
     * Get an order belonging to the authenticated customer.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse getOrderById(
            @PathVariable Long orderId
    ) {
        Long userId = currentUserService.getCurrentUserId();

        return orderService.getOrderByIdForUser(orderId, userId);
    }

    /**
     * Get orders belonging to the authenticated customer.
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<OrderResponse> getMyOrders() {
        Long userId = currentUserService.getCurrentUserId();
        return orderService.getOrdersByUserId(userId);
    }

    /**
     * Admin-only access to all orders.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    /**
     * Restaurant owner accepts an order.
     */
    @PatchMapping("/{orderId}/accept")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public OrderResponse acceptOrder(
            @PathVariable Long orderId
    ) {
        return orderService.acceptOrder(orderId);
    }

    /**
     * Restaurant owner rejects an order.
     */
    @PatchMapping("/{orderId}/reject")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public OrderResponse rejectOrder(
            @PathVariable Long orderId
    ) {
        return orderService.rejectOrder(orderId);
    }

    /**
     * Restaurant owner starts preparing an order.
     */
    @PatchMapping("/{orderId}/preparing")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public OrderResponse startPreparing(
            @PathVariable Long orderId
    ) {
        return orderService.startPreparing(orderId);
    }

    /**
     * Restaurant owner marks an order ready for pickup.
     */
    @PatchMapping("/{orderId}/ready")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public OrderResponse markReadyForPickup(
            @PathVariable Long orderId
    ) {
        return orderService.markReadyForPickup(orderId);
    }

    /**
     * Admin or restaurant owner assigns a delivery partner.
     */
    @PatchMapping("/{orderId}/assign-delivery")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public OrderResponse assignDeliveryPartner(
            @PathVariable Long orderId,
            @RequestParam Long deliveryPartnerId
    ) {
        return orderService.assignDeliveryPartner(
                orderId,
                deliveryPartnerId
        );
    }

    /**
     * Delivery partner marks an order as delivered.
     */
    @PatchMapping("/{orderId}/delivered")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public OrderResponse markDelivered(
            @PathVariable Long orderId
    ) {
        return orderService.markDelivered(orderId);
    }

    /**
     * Customer cancels their own order.
     */
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse cancelOrder(
            @PathVariable Long orderId
    ) {
        Long userId = currentUserService.getCurrentUserId();

        return orderService.cancelOrder(orderId, userId);
    }
}
