package com.foodapp.foodhub.repository;

import com.foodapp.foodhub.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPaymobOrderId(String paymobOrderId);
}

