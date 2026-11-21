
package com.project.cravely.models;

import com.project.cravely.enums.OrderStatus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderLifecycleTest {

    private Order createOrder() {
        return Order.builder()
                .userId(1L)
                .restaurantId(1L)
                .status(OrderStatus.CREATED)
                .build();
    }

    @Test
    void shouldCompleteOrderLifecycleSuccessfully() {

        Order order = createOrder();

        order.acceptByRestaurant();
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());

        order.startPreparing();
        assertEquals(OrderStatus.PREPARING, order.getStatus());

        order.markReadyForPickup();
        assertEquals(OrderStatus.READY_FOR_PICKUP, order.getStatus());

        order.markOutForDelivery(101L);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());
        assertEquals(101L, order.getDeliveryPartnerId());

        order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void shouldRejectOrderFromCreatedStatus() {

        Order order = createOrder();

        order.rejectByRestaurant();

        assertEquals(OrderStatus.REJECTED, order.getStatus());
    }

    @Test
    void shouldNotAcceptOrderTwice() {

        Order order = createOrder();

        order.acceptByRestaurant();

        assertThrows(
                IllegalStateException.class,
                order::acceptByRestaurant
        );
    }

    @Test
    void shouldNotStartPreparingBeforeAcceptance() {

        Order order = createOrder();

        assertThrows(
                IllegalStateException.class,
                order::startPreparing
        );

        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void shouldNotAssignDeliveryPartnerBeforeOrderIsReady() {

        Order order = createOrder();

        assertThrows(
                IllegalStateException.class,
                () -> order.markOutForDelivery(101L)
        );

        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void shouldRequireDeliveryPartnerId() {

        Order order = createOrder();

        order.acceptByRestaurant();
        order.startPreparing();
        order.markReadyForPickup();

        assertThrows(
                IllegalArgumentException.class,
                () -> order.markOutForDelivery(null)
        );

        assertEquals(
                OrderStatus.READY_FOR_PICKUP,
                order.getStatus()
        );
    }

    @Test
    void shouldNotDeliverOrderBeforeOutForDelivery() {

        Order order = createOrder();

        assertThrows(
                IllegalStateException.class,
                order::markDelivered
        );
    }

    @Test
    void shouldAllowCancellationOfCreatedOrder() {

        Order order = createOrder();

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldNotCancelDeliveredOrder() {

        Order order = createOrder();

        order.acceptByRestaurant();
        order.startPreparing();
        order.markReadyForPickup();
        order.markOutForDelivery(101L);
        order.markDelivered();

        assertThrows(
                IllegalStateException.class,
                order::cancel
        );

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void shouldNotCancelRejectedOrder() {

        Order order = createOrder();

        order.rejectByRestaurant();

        assertThrows(
                IllegalStateException.class,
                order::cancel
        );
    }

    @Test
    void shouldNotCancelAlreadyCancelledOrder() {

        Order order = createOrder();

        order.cancel();

        assertThrows(
                IllegalStateException.class,
                order::cancel
        );
    }
}
