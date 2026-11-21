
package com.project.cravely.services;

import com.project.cravely.dto.MenuItemResponse;
import com.project.cravely.models.MenuItem;
import com.project.cravely.models.Restaurant;
import com.project.cravely.repositories.MenuItemRepository;
import com.project.cravely.repositories.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final S3Service s3Service;

    public MenuItemResponse addMenuItem(MenuItem menuItem) {
        validateRestaurant(menuItem.getRestaurantId());

        MenuItem savedItem = menuItemRepository.save(menuItem);
        return toResponse(savedItem);
    }

    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Menu item not found with id: " + id
                        )
                );

        return toResponse(menuItem);
    }

    public List<MenuItemResponse> getRestaurantMenu(Long restaurantId) {
        validateRestaurant(restaurantId);

        return menuItemRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MenuItemResponse> getAvailableMenuItems(Long restaurantId) {
        validateRestaurant(restaurantId);

        return menuItemRepository.findAvailableByRestaurantId(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteMenuItem(Long id) {
        getMenuItemEntity(id);
        menuItemRepository.deleteById(id);
    }

    private MenuItem getMenuItemEntity(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Menu item not found with id: " + id
                        )
                );
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getRestaurantId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                s3Service.generatePresignedUrl(item.getImageKey()),
                item.getCategory(),
                item.getPreparationTimeMinutes(),
                item.isVegetarian(),
                item.isAvailable()
        );
    }

    private Restaurant validateRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Restaurant not found with id: " + restaurantId
                        )
                );
    }

    public MenuItemResponse uploadMenuItemImage(
            Long menuItemId,
            MultipartFile file
    ) {
        MenuItem item = getMenuItemEntity(menuItemId);

        String imageKey = s3Service.uploadImage(file);

        item.updateImageKey(imageKey);

        MenuItem savedItem = menuItemRepository.save(item);

        return toResponse(savedItem);
    }
}
