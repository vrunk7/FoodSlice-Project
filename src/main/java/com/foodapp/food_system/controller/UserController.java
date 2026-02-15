package com.foodapp.food_system.controller;
import java.util.Map;
import java.util.HashMap;
import com.foodapp.food_system.model.User;
import com.foodapp.food_system.repository.WishlistRepository;
import com.foodapp.food_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // Note: Use @Controller for Thymeleaf, not @RestController
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.foodapp.food_system.model.Product; // Import the Product class we made earlier
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WishlistRepository wishlistRepository;
    
    @GetMapping("/")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        User user = userRepository.findByUsername(username); 
        
        if (user != null && user.getPassword().equals(password)) {
            // ROLE-BASED CHECK:
            if ("ADMIN".equals(user.getRole())) {
                // If the name is exactly 'admin', send them to the Admin Panel
                return "redirect:/admin/dashboard?username=" + username;
            } else {
                // Everyone else goes to the regular Food Dashboard
                return "redirect:/dashboard?username=" + username;
            }
        }
        
        model.addAttribute("error", "Invalid Credentials");
        return "login";
    }
    
    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(required = false) String username,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String category,
                                Model model) {
    	    	
        // --- DEBUG PRINT 1: Check what came from the browser ---
        System.out.println("--- DASHBOARD DEBUG START ---");
        System.out.println("Username: " + username);
        System.out.println("Search Term: " + search);
        System.out.println("Category Selected: " + category);

        if (username == null) username = "joshua"; 
        
        // 2. FETCH THE USER OBJECT (This is the missing part!)
        User user = userRepository.findByUsername(username); 

        // 3. Send data to Model
        model.addAttribute("username", username);
        model.addAttribute("role", user.getRole());
        
        // Safety check: if user is found, send balance; otherwise, send 0.0
        if (user != null) {
            model.addAttribute("walletBalance", user.getWalletBalance() != null ? user.getWalletBalance() : 0.0);
            model.addAttribute("userAddress", user.getAddress());
        }        
        
       // model.addAttribute("username", username);
        model.addAttribute("walletBalance", user.getWalletBalance());
        String url = "http://localhost:8082/api/products";
        Product[] allProducts = restTemplate.getForObject(url, Product[].class);
        
        List<Product> filteredList = new ArrayList<>();
        
        if (allProducts != null) {
            System.out.println("Total Products Found: " + allProducts.length); // Debug Print 2
            
            for (Product p : allProducts) {
                boolean matches = true;
                
                // Debug Print 3: See what we are comparing
                // System.out.println("Checking: " + p.getName() + " [" + p.getCategory() + "]"); 

                // Search Filter
                if (search != null && !search.isEmpty()) {
                    String keyword = search.toLowerCase();
                    if (!p.getName().toLowerCase().contains(keyword) && 
                        !p.getDescription().toLowerCase().contains(keyword)) {
                        matches = false;
                    }
                }

                // Category Filter
                if (category != null && !category.isEmpty() && !category.equals("All")) {
                    if (p.getCategory() == null || !p.getCategory().equalsIgnoreCase(category)) {
                        matches = false;
                    }
                }

                if (matches) {
                    filteredList.add(p);
                }
            }
        }
        
        System.out.println("Products Left After Filter: " + filteredList.size()); // Debug Print 4
        System.out.println("-----------------------------");

        model.addAttribute("products", filteredList);
        model.addAttribute("currentCategory", category != null ? category : "All");
        
        java.util.List<com.foodapp.food_system.model.Wishlist> userWishlist = wishlistRepository.findByUsername(username);
        
        // Create a simple list of IDs (e.g., [1, 5, 8]) that are in the wishlist
        java.util.List<Long> wishlistProductIds = new java.util.ArrayList<>();
        for (com.foodapp.food_system.model.Wishlist w : userWishlist) {
            wishlistProductIds.add(w.getProductId());
        }
        
        // Send this list to HTML
        model.addAttribute("wishlistProductIds", wishlistProductIds);

        return "dashboard";
    }
    
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(@RequestParam String username, Model model) {
    	User user = userRepository.findByUsername(username);
        // SECURITY CHECK: If someone tries to type the URL manually but isn't admin
        if (!"ADMIN".equals(user.getRole())) {
            return "redirect:/dashboard?username=" + username;
        }

        // Fetch products from Product Service (Port 8082)
        String url = "http://localhost:8082/api/products";
        Product[] products = restTemplate.getForObject(url, Product[].class);
        
        model.addAttribute("products", products);
        model.addAttribute("username", username);
        
        return "admin-dashboard"; // We will create this HTML file next
    }
 
 // Handle Adding OR Updating a Product
    @PostMapping("/admin/add-product")
    public String addProduct(@RequestParam String username, 
                             @ModelAttribute Product product) { // Binds form fields to Product object
        
        // Debugging: Print what we are sending
        System.out.println("Admin Action on: " + product.getName());
        System.out.println("Has ID? " + (product.getId() != null ? "Yes (Update)" : "No (New)"));

        // Call Product Service (Port 8082)
        String url = "http://localhost:8082/api/products";
        restTemplate.postForObject(url, product, Product.class);
        
        return "redirect:/admin/dashboard?username=" + username + "&msg=Action Successful!";
    }

    // Handle Deleting a Product
    @GetMapping("/admin/delete/{id}")
    public String deleteProduct(@PathVariable Long id, @RequestParam String username) {
        String url = "http://localhost:8082/api/products/" + id;
        restTemplate.delete(url);
        
        return "redirect:/admin/dashboard?username=" + username + "&msg=Item Deleted!";
    }
    
 // 1. Show the Order Manager Page
    @GetMapping("/admin/orders")
    public String adminOrders(@RequestParam String username, Model model) {
        // SECURITY: Only Admin Allowed
        User user = userRepository.findByUsername(username);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/dashboard?username=" + username;
        }

        // Call Order Service (Port 8083) to get EVERYTHING
        String url = "http://localhost:8083/api/orders";
        try {
            // We use List.class because we expect a big list of orders
            List orders = restTemplate.getForObject(url, List.class);
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("error", "Order Service is Offline!");
        }

        model.addAttribute("username", username);
        return "admin-orders";
    }

    // 2. Handle Status Change (e.g., Click "Mark as Delivered")
    @GetMapping("/admin/order-status/{id}")
    public String updateOrderStatus(@PathVariable Long id, 
                                    @RequestParam String status, 
                                    @RequestParam String username) {
        // Call Order Service to update
        String url = "http://localhost:8083/api/orders/" + id + "/status?status=" + status;
        restTemplate.postForObject(url, null, Object.class);
        
        return "redirect:/admin/orders?username=" + username + "&msg=Order Updated!";
    }
  /*  @PostMapping("/order")
    public String placeOrder(@RequestParam String username, 
                             @RequestParam Double price, 
                             @RequestParam String paymentMethod,
                             @RequestParam(defaultValue = "1") Integer quantity, // NEW PARAMETER
                             Model model) {
        
        // 1. Find User
        User user = userRepository.findByUsername(username);
        
        // 2. Calculate Total (Price * Quantity)
        Double totalAmount = price * quantity;

        // 3. Create Order Data
        java.util.Map<String, Object> orderData = new java.util.HashMap<>();
        orderData.put("userId", user.getId());
        orderData.put("totalAmount", totalAmount);
        
        // 4. Call Order Service
        String orderUrl = "http://localhost:8083/api/orders";
        restTemplate.postForObject(orderUrl, orderData, Object.class);
        
     // 1. PAYMENT LOGIC
        if ("UPI".equals(paymentMethod) || "CARD".equals(paymentMethod)) {
            // Mock success for external payments
            System.out.println("Processing external payment via: " + paymentMethod);
        } 
        else if ("CASH".equals(paymentMethod)) {
            // It's COD, we don't touch the wallet balance!
            System.out.println("Order set as Cash on Delivery");
        } 
        else {
            // Assume WALLET (if you add a wallet button) or default behavior
            if (user.getWalletBalance() < price) {
                return "redirect:/dashboard?username=" + username + "&error=Insufficient Wallet Balance";
            }
            user.setWalletBalance(user.getWalletBalance() - price);
            userRepository.save(user);
        }
        
     // 2. CALL ORDER SERVICE (Same as before)
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", user.getId());
        orderData.put("totalAmount", price);
        orderData.put("status", "CASH".equals(paymentMethod) ? "PENDING" : "PAID");
        
     // Inside placeOrder method
        user.setWalletBalance(user.getWalletBalance() - totalAmount);
        userRepository.save(user); // Now it's a permanent subtraction!
        
        restTemplate.postForObject("http://localhost:8083/api/orders", orderData, Object.class);
        
        return "redirect:/dashboard?username=" + username + "&msg=Order Placed Successfully!";
    }*/
    
    @PostMapping("/order")
    public String placeOrder(@RequestParam String username, 
                             @RequestParam Double price, 
                             @RequestParam String paymentMethod,
                             @RequestParam(defaultValue = "1") Integer quantity, 
                             Model model) {
        
        // 1. Find User
        User user = userRepository.findByUsername(username);
        if (user == null) return "redirect:/";

        // 2. Calculate Total
        Double totalAmount = price * quantity;
        String orderStatus = "PAID";

        // 3. PAYMENT LOGIC (Branching)
        if ("CASH".equals(paymentMethod)) {
            System.out.println("Order set as Cash on Delivery");
            orderStatus = "PENDING";
        } 
        else if ("CARD".equals(paymentMethod) || "UPI".equals(paymentMethod)) {
            System.out.println("Processing external payment via: " + paymentMethod);
        } 
        else {
            // Default: WALLET logic
            if (user.getWalletBalance() < totalAmount) {
                return "redirect:/dashboard?username=" + username + "&error=Insufficient Wallet Balance";
            }
            user.setWalletBalance(user.getWalletBalance() - totalAmount);
            userRepository.save(user); // Persistent subtraction
        }

        // 4. PREPARE DATA FOR ORDER SERVICE
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", user.getId());
        orderData.put("totalAmount", totalAmount);
        orderData.put("status", orderStatus);
        
        // 5. CALL ORDER SERVICE (Port 8083) - Only call it ONCE
        try {
            String orderUrl = "http://localhost:8083/api/orders";
            restTemplate.postForObject(orderUrl, orderData, Object.class);
        } catch (Exception e) {
            System.out.println("Order Service Error: " + e.getMessage());
        }
        
        return "redirect:/dashboard?username=" + username + "&msg=Order Placed via " + paymentMethod;
    }
    
    @GetMapping("/my-orders")
    public String myOrders(@RequestParam(required = false) String username, Model model) {
        // Fallback: If no username is in the URL, use our default user
        if (username == null) {
            username = "joshua"; 
        }

        User user = userRepository.findByUsername(username);
        
        if (user != null) {
            // 1. Call Order Service (8083) to get history
            String url = "http://localhost:8083/api/orders/user/" + user.getId();
            
            try {
                // Fetch the list of orders
                java.util.List orders = restTemplate.getForObject(url, java.util.List.class);
                model.addAttribute("orders", orders);
            } catch (Exception e) {
                model.addAttribute("error", "Order Service is down! Could not fetch history.");
            }
        }
        
        model.addAttribute("username", username);
        return "my-orders"; // This looks for my-orders.html
    }
    
    @PostMapping("/wallet/add")
    public String addMoney(@RequestParam String username, @RequestParam Double amount) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            // Update balance in database
            user.setWalletBalance(user.getWalletBalance() + amount);
            userRepository.save(user);
        }
        return "redirect:/dashboard?username=" + username + "&msg=Wallet Updated!";
    }
} 