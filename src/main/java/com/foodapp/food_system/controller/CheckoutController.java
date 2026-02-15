package com.foodapp.food_system.controller;

import com.foodapp.food_system.model.User;
import com.foodapp.food_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CheckoutController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam String username, Model model) {
        User user = userRepository.findByUsername(username);
        model.addAttribute("user", user);
        model.addAttribute("username", username);
        return "checkout";
    }
}