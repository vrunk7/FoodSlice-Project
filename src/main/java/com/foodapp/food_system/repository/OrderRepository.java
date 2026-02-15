package com.foodapp.food_system.repository;

import com.foodapp.food_system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId); // Find all orders for one person
}