package com.foodapp.food_system.repository;

import com.foodapp.food_system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}