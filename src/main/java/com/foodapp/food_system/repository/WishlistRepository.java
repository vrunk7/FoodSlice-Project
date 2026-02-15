package com.foodapp.food_system.repository;

import com.foodapp.food_system.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUsername(String username);
    Wishlist findByUsernameAndProductId(String username, Long productId);
    void deleteByUsernameAndProductId(String username, Long productId);
}