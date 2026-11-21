
package com.project.cravely.services;

import com.project.cravely.models.Cart;
import com.project.cravely.models.CartItem;
import com.project.cravely.models.MenuItem;
import com.project.cravely.models.Restaurant;
import com.project.cravely.repositories.CartRepository;
import com.project.cravely.repositories.MenuItemRepository;
import com.project.cravely.repositories.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public Cart addItem(Long userId, Long menuItemId, int quantity) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (menuItemId == null || menuItemId <= 0) {
            throw new IllegalArgumentException("Invalid menu item ID");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found")
                );

        Restaurant restaurant = restaurantRepository.findById(
                menuItem.getRestaurantId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Restaurant not found")
        );

        if (!restaurant.isActive()) {
            throw new IllegalStateException(
                    "Restaurant is currently inactive"
            );
        }

        if (!menuItem.isAvailable()) {
            throw new IllegalStateException(
                    "Menu item is currently unavailable"
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder()
                        .userId(userId)
                        .build());

        CartItem cartItem = CartItem.builder()
                .menuItemId(menuItem.getId())
                .name(menuItem.getName())
                .price(menuItem.getPrice())
                .quantity(quantity)
                .build();

        cart.addItem(cartItem, menuItem.getRestaurantId());

        return cartRepository.saveAndFlush(cart);
    }

    @Transactional(readOnly = true)
    public Cart getCart(Long userId) {

        validateUserId(userId);

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder()
                        .userId(userId)
                        .build());
    }

    public Cart updateQuantity(
            Long userId,
            Long menuItemId,
            int quantity
    ) {

        validateUserId(userId);

        if (menuItemId == null || menuItemId <= 0) {
            throw new IllegalArgumentException("Invalid menu item ID");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Cart cart = getExistingCart(userId);

        // Validate that the menu item is still available.
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found")
                );

        if (!menuItem.isAvailable()) {
            throw new IllegalStateException(
                    "Menu item is currently unavailable"
            );
        }

        cart.updateQuantity(menuItemId, quantity);

        return cartRepository.saveAndFlush(cart);
    }

    public Cart removeItem(Long userId, Long menuItemId) {

        validateUserId(userId);

        if (menuItemId == null || menuItemId <= 0) {
            throw new IllegalArgumentException("Invalid menu item ID");
        }

        Cart cart = getExistingCart(userId);

        cart.removeItem(menuItemId);

        return cartRepository.saveAndFlush(cart);
    }

    public void clearCart(Long userId) {

        validateUserId(userId);

        cartRepository.deleteByUserId(userId);
    }

    private Cart getExistingCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cart not found")
                );
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }
}
