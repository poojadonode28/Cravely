
package com.project.cravely.controllers;

import com.project.cravely.models.Cart;
import com.project.cravely.services.CartService;
import com.project.cravely.services.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @PostMapping("/items")
    public Cart addItem(
            @RequestParam Long menuItemId,
            @RequestParam int quantity
    ) {
        Long userId = currentUserService.getCurrentUserId();

        return cartService.addItem(userId, menuItemId, quantity);
    }

    @GetMapping
    public Cart getCart() {
        Long userId = currentUserService.getCurrentUserId();

        return cartService.getCart(userId);
    }

    @PutMapping("/items/{menuItemId}")
    public Cart updateQuantity(
            @PathVariable Long menuItemId,
            @RequestParam int quantity
    ) {
        Long userId = currentUserService.getCurrentUserId();

        return cartService.updateQuantity(userId, menuItemId, quantity);
    }

    @DeleteMapping("/items/{menuItemId}")
    public Cart removeItem(
            @PathVariable Long menuItemId
    ) {
        Long userId = currentUserService.getCurrentUserId();

        return cartService.removeItem(userId, menuItemId);
    }

    @DeleteMapping
    public void clearCart() {
        Long userId = currentUserService.getCurrentUserId();

        cartService.clearCart(userId);
    }
}
