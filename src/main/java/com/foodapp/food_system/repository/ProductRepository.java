package com.foodapp.food_system.repository;

import com.foodapp.food_system.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}