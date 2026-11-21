
package com.project.cravely.services;

import com.project.cravely.models.Restaurant;
import com.project.cravely.repositories.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final S3Service s3Service;

    // Create restaurant
    public Restaurant createRestaurant(Restaurant restaurant) {
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return enrichImageUrl(savedRestaurant);
    }

    // Get restaurant by ID
    public Restaurant getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Restaurant not found with id: " + id
                        )
                );

        return enrichImageUrl(restaurant);
    }

    // Get all restaurants
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::enrichImageUrl)
                .toList();
    }

    // Get active restaurants
    public List<Restaurant> getAllActiveRestaurants() {
        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(this::enrichImageUrl)
                .toList();
    }

    // Upload restaurant cover image to S3
    @Transactional
    public Restaurant uploadRestaurantImage(
            Long restaurantId,
            MultipartFile file
    ) {

        // Step 1: Find restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Restaurant not found with id: " + restaurantId
                        )
                );

        // Step 2: Upload image to AWS S3
        String imageKey = s3Service.uploadImage(file);

        // Step 3: Store S3 image key
        restaurant.setImageKey(imageKey);

        // Step 4: Save restaurant
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // Step 5: Generate presigned image URL
        return enrichImageUrl(savedRestaurant);
    }

    // Delete restaurant
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Restaurant not found with id: " + id
                        )
                );

        restaurantRepository.delete(restaurant);
    }

    // Add presigned image URL to response
    private Restaurant enrichImageUrl(Restaurant restaurant) {

        String imageKey = restaurant.getImageKey();

        if (imageKey != null && !imageKey.isBlank()) {
            restaurant.setImageUrl(
                    s3Service.generatePresignedUrl(imageKey)
            );
        } else {
            restaurant.setImageUrl(null);
        }

        return restaurant;
    }
}
