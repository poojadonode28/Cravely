
package com.project.cravely.services;

import com.project.cravely.dto.OrderResponse;
import com.project.cravely.enums.DeliveryPartnerStatus;
import com.project.cravely.enums.OrderStatus;
import com.project.cravely.models.Cart;
import com.project.cravely.models.CartItem;
import com.project.cravely.models.DeliveryPartner;
import com.project.cravely.models.MenuItem;
import com.project.cravely.models.Order;
import com.project.cravely.models.OrderItem;
import com.project.cravely.models.Restaurant;
import com.project.cravely.repositories.CartRepository;
import com.project.cravely.repositories.DeliveryPartnerRepository;
import com.project.cravely.repositories.MenuItemRepository;
import com.project.cravely.repositories.OrderRepository;
import com.project.cravely.repositories.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    /**
     * Place an order using the customer's cart.
     */
    public OrderResponse placeOrder(Long userId) {

        validateUserId(userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cart not found")
                );

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Restaurant restaurant = restaurantRepository
                .findById(cart.getRestaurantId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Restaurant not found")
                );

        if (!restaurant.isActive()) {
            throw new IllegalStateException(
                    "Restaurant is currently inactive"
            );
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            if (cartItem.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Invalid item quantity"
                );
            }

            MenuItem menuItem = menuItemRepository
                    .findById(cartItem.getMenuItemId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Menu item not found: "
                                            + cartItem.getMenuItemId()
                            )
                    );

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException(
                        menuItem.getName() + " is unavailable"
                );
            }

            if (!restaurant.getId().equals(menuItem.getRestaurantId())) {
                throw new IllegalArgumentException(
                        "Menu item does not belong to cart restaurant"
                );
            }

            BigDecimal price = menuItem.getPrice();

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "Invalid menu item price: " + menuItem.getId()
                );
            }

            OrderItem orderItem = OrderItem.builder()
                    .menuItemId(menuItem.getId())
                    .name(menuItem.getName())
                    .price(price)
                    .quantity(cartItem.getQuantity())
                    .build();

            orderItems.add(orderItem);

            BigDecimal itemTotal = price.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            totalAmount = totalAmount.add(itemTotal);
        }

        Order order = Order.builder()
                .userId(userId)
                .restaurantId(restaurant.getId())
                .items(List.copyOf(orderItems))
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .build();

        Order savedOrder = orderRepository.saveAndFlush(order);

        cartRepository.deleteByUserId(userId);

        return OrderResponse.from(savedOrder);
    }

    /**
     * Restaurant accepts an order.
     */
    public OrderResponse acceptOrder(Long orderId) {

        Order order = findOrderEntity(orderId);

        order.acceptByRestaurant();

        return saveOrder(order);
    }

    /**
     * Restaurant rejects an order.
     */
    public OrderResponse rejectOrder(Long orderId) {

        Order order = findOrderEntity(orderId);

        order.rejectByRestaurant();

        return saveOrder(order);
    }

    /**
     * Restaurant starts preparing an order.
     */
    public OrderResponse startPreparing(Long orderId) {

        Order order = findOrderEntity(orderId);

        order.startPreparing();

        return saveOrder(order);
    }

    /**
     * Restaurant marks an order ready for pickup.
     */
    public OrderResponse markReadyForPickup(Long orderId) {

        Order order = findOrderEntity(orderId);

        order.markReadyForPickup();

        return saveOrder(order);
    }

    /**
     * Assign an available delivery partner to an order.
     */
    public OrderResponse assignDeliveryPartner(
            Long orderId,
            Long deliveryPartnerId
    ) {

        if (deliveryPartnerId == null || deliveryPartnerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid delivery partner ID"
            );
        }

        Order order = findOrderEntity(orderId);

        DeliveryPartner partner = deliveryPartnerRepository
                .findById(deliveryPartnerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Delivery partner not found: "
                                        + deliveryPartnerId
                        )
                );

        if (partner.getStatus() != DeliveryPartnerStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Delivery partner is not available"
            );
        }

        order.markOutForDelivery(deliveryPartnerId);
        partner.markOnDelivery();

        deliveryPartnerRepository.saveAndFlush(partner);

        return saveOrder(order);
    }

    /**
     * Mark an order as delivered.
     */
    public OrderResponse markDelivered(Long orderId) {

        Order order = findOrderEntity(orderId);

        Long partnerId = order.getDeliveryPartnerId();

        if (partnerId == null) {
            throw new IllegalStateException(
                    "No delivery partner is assigned to this order"
            );
        }

        DeliveryPartner partner = deliveryPartnerRepository
                .findById(partnerId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Assigned delivery partner not found"
                        )
                );

        order.markDelivered();
        partner.markAvailable();

        deliveryPartnerRepository.saveAndFlush(partner);

        return saveOrder(order);
    }

    /**
     * Cancel an order belonging to a specific customer.
     */
    public OrderResponse cancelOrder(
            Long orderId,
            Long userId
    ) {

        validateUserId(userId);

        Order order = findOrderEntity(orderId);

        verifyOrderOwnership(order, userId);

        return cancelOrderInternal(order);
    }

    /**
     * Existing internal cancellation operation.
     * Restrict its use to trusted administrative workflows.
     */
    public OrderResponse cancelOrder(Long orderId) {

        Order order = findOrderEntity(orderId);

        return cancelOrderInternal(order);
    }

    private OrderResponse cancelOrderInternal(Order order) {

        Long partnerId = order.getDeliveryPartnerId();

        order.cancel();

        if (partnerId != null) {

            DeliveryPartner partner = deliveryPartnerRepository
                    .findById(partnerId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Assigned delivery partner not found"
                            )
                    );

            if (partner.getStatus() == DeliveryPartnerStatus.ON_DELIVERY) {
                partner.markAvailable();
                deliveryPartnerRepository.saveAndFlush(partner);
            }
        }

        return saveOrder(order);
    }

    /**
     * Get an order belonging to a specific customer.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForUser(
            Long orderId,
            Long userId
    ) {

        validateUserId(userId);

        Order order = findOrderEntity(orderId);

        verifyOrderOwnership(order, userId);

        return OrderResponse.from(order);
    }

    /**
     * Get an order by ID.
     * Use only for trusted administrative/operational workflows.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {

        Order order = findOrderEntity(orderId);

        return OrderResponse.from(order);
    }

    /**
     * Get all orders belonging to a customer.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {

        validateUserId(userId);

        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * Get all orders.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    private OrderResponse saveOrder(Order order) {

        Order savedOrder = orderRepository.saveAndFlush(order);

        return OrderResponse.from(savedOrder);
    }

    private Order findOrderEntity(Long orderId) {

        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + orderId
                        )
                );
    }

    private void verifyOrderOwnership(
            Order order,
            Long userId
    ) {

        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException(
                    "You are not allowed to access this order"
            );
        }
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }
}
