package com.foodapp.food_system.controller;

import com.foodapp.food_system.model.Wishlist;
import com.foodapp.food_system.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // Use Controller for HTML
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    // 1. VIEW WISHLIST PAGE
    @GetMapping("/wishlist")
    public String viewWishlist(@RequestParam(required = false) String username, Model model) {
        if (username == null) username = "joshua";
        model.addAttribute("username", username);
        model.addAttribute("wishlistItems", wishlistRepository.findByUsername(username));
        return "wishlist";
    }

    // 2. TOGGLE API (Used by the Heart Button via JavaScript)
    @PostMapping("/api/wishlist/toggle")
    @ResponseBody // Return JSON/Text, not a HTML page
    @Transactional // Required for delete operation
    public String toggleWishlist(@RequestBody Wishlist item) {
        
        Wishlist existing = wishlistRepository.findByUsernameAndProductId(item.getUsername(), item.getProductId());
        
        if (existing != null) {
            // If exists, delete it (Unlike)
            wishlistRepository.delete(existing);
            return "REMOVED";
        } else {
            // If not exists, save it (Like)
            wishlistRepository.save(item);
            return "ADDED";
        }
    }
}