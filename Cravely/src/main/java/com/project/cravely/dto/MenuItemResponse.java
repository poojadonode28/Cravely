
package com.project.cravely.dto;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        Long restaurantId,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        String category,
        Integer preparationTimeMinutes,
        boolean vegetarian,
        boolean available
) {
}
