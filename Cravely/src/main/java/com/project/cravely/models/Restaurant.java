package com.project.cravely.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String address;

    private double latitude;

    private double longitude;

    @Builder.Default
    private boolean active = true;

    @Column(name = "image_key", length = 500)
    private String imageKey;

    @Transient
    private String imageUrl;
}