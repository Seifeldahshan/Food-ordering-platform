package com.foodapp.foodhub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String phone;
    private String imageUrl;
    private String address;
    private Double rating = 0.0;

    @ManyToOne
    @JoinColumn(name = "subcategory_id", nullable = false)
    private Subcategory subcategory;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "restaurant_zone",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "zone_id")
    )
    private Set<Zone> zones = new HashSet<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<RestaurantSchedule> restaurantSchedule = new ArrayList<>();

}