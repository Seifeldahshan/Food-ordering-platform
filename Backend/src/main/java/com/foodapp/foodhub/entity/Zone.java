package com.foodapp.foodhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Zone extends BaseEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToMany(mappedBy = "zones")
    private Set<Restaurant> restaurants = new HashSet<>();

    @OneToMany(mappedBy = "zone")
    private Set<UserAddress> addresses = new HashSet<>();
}

