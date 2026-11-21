
package com.project.cravely.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "menu_items",
        indexes = {
                @Index(
                        name = "idx_menu_items_restaurant_id",
                        columnList = "restaurant_id"
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_key", length = 500)
    private String imageKey;

    @Column(length = 100)
    private String category;

    @Column(name = "preparation_time_minutes")
    private Integer preparationTimeMinutes;

    @Builder.Default
    @Column(nullable = false)
    private boolean vegetarian = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;

    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }
}
