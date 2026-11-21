
package com.project.cravely.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_user_id",
                        columnNames = "user_id"
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "cart_items",
            joinColumns = @JoinColumn(name = "cart_id")
    )
    @OrderColumn(name = "item_position")
    private List<CartItem> items = new ArrayList<>();

    public void addItem(CartItem item, Long restaurantId) {

        if (this.restaurantId != null &&
                !this.restaurantId.equals(restaurantId)) {
            throw new IllegalArgumentException(
                    "Cart cannot contain items from different restaurants"
            );
        }

        this.restaurantId = restaurantId;

        for (int i = 0; i < items.size(); i++) {
            CartItem existingItem = items.get(i);

            if (existingItem.getMenuItemId().equals(item.getMenuItemId())) {
                CartItem updatedItem = CartItem.builder()
                        .menuItemId(existingItem.getMenuItemId())
                        .name(existingItem.getName())
                        .price(existingItem.getPrice())
                        .quantity(existingItem.getQuantity() + item.getQuantity())
                        .build();

                items.set(i, updatedItem);
                return;
            }
        }

        items.add(item);
    }

    public void updateQuantity(Long menuItemId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);

            if (item.getMenuItemId().equals(menuItemId)) {
                items.set(i, CartItem.builder()
                        .menuItemId(item.getMenuItemId())
                        .name(item.getName())
                        .price(item.getPrice())
                        .quantity(quantity)
                        .build());
                return;
            }
        }

        throw new IllegalArgumentException("Item not found in cart");
    }

    public void removeItem(Long menuItemId) {
        items.removeIf(item ->
                item.getMenuItemId().equals(menuItemId)
        );

        if (items.isEmpty()) {
            restaurantId = null;
        }
    }

    public void clearCart() {
        items.clear();
        restaurantId = null;
    }
}
