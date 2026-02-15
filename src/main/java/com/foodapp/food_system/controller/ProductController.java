package com.foodapp.food_system.controller;

import com.foodapp.food_system.model.Product;
import com.foodapp.food_system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products") // <--- This sets the base URL
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // 1. GET ALL (You likely already have this)
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 2. ADD / UPDATE (This is the new part)
    // notice: No path inside @PostMapping because it inherits "/api/products"
    @PostMapping 
    public Product addProduct(@RequestBody Product product) {
        // If product has an ID, .save() updates it.
        // If product ID is null, .save() creates a new row.
        return productRepository.save(product);
    }
    
    // 3. DELETE (We need this for the "Trash" icon)
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}
