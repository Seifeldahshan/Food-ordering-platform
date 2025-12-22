package com.foodapp.foodhub.repository;

import com.foodapp.foodhub.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}
