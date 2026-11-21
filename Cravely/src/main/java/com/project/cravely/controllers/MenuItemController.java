
package com.project.cravely.controllers;

import com.project.cravely.dto.MenuItemResponse;
import com.project.cravely.models.MenuItem;
import com.project.cravely.services.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping("/menu-items")
    public MenuItemResponse addMenuItem(@RequestBody MenuItem menuItem) {
        return menuItemService.addMenuItem(menuItem);
    }

    @GetMapping("/menu-items/{id}")
    public MenuItemResponse getMenuItem(@PathVariable Long id) {
        return menuItemService.getMenuItemById(id);
    }

    @GetMapping("/restaurants/{restaurantId}/menu")
    public List<MenuItemResponse> getRestaurantMenu(
            @PathVariable Long restaurantId
    ) {
        return menuItemService.getRestaurantMenu(restaurantId);
    }

    @GetMapping("/restaurants/{restaurantId}/menu/available")
    public List<MenuItemResponse> getAvailableMenuItems(
            @PathVariable Long restaurantId
    ) {
        return menuItemService.getAvailableMenuItems(restaurantId);
    }

    @DeleteMapping("/menu-items/{id}")
    public void deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
    }

    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @PostMapping(
            value = "/menu-items/{id}/image",
            consumes = "multipart/form-data"
    )
    public MenuItemResponse uploadMenuItemImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return menuItemService.uploadMenuItemImage(id, file);
    }
}
