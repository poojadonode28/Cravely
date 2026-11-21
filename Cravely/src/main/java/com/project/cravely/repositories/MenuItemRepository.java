package com.project.cravely.repositories;

import com.project.cravely.models.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantId(Long restaurantId);

    Optional<MenuItem> findByNameIgnoreCase(String name);

    @Query("""
        SELECT m
        FROM MenuItem m
        WHERE m.restaurantId = :restaurantId
          AND m.available = true
    """)
    List<MenuItem> findAvailableByRestaurantId(
            @Param("restaurantId") Long restaurantId
    );
}