
package com.project.cravely.controllers;

import com.project.cravely.models.Restaurant;
import com.project.cravely.services.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // Create restaurant
    @PostMapping
    public Restaurant createRestaurant(
            @RequestBody Restaurant restaurant
    ) {
        return restaurantService.createRestaurant(restaurant);
    }

    // Get restaurant by ID
    @GetMapping("/{id}")
    public Restaurant getRestaurantById(
            @PathVariable Long id
    ) {
        return restaurantService.getRestaurantById(id);
    }

    // Get all restaurants
    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    // Get all active restaurants
    @GetMapping("/active")
    public List<Restaurant> getAllActiveRestaurants() {
        return restaurantService.getAllActiveRestaurants();
    }

    // Upload restaurant cover image
    @PutMapping(
            value = "/{id}/image",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public Restaurant uploadRestaurantImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return restaurantService.uploadRestaurantImage(id, file);
    }

    // Delete restaurant
    @DeleteMapping("/{id}")
    public void deleteRestaurant(
            @PathVariable Long id
    ) {
        restaurantService.deleteRestaurant(id);
    }
}
