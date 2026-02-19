package com.foodapp.food_system.controller;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import com.foodapp.food_system.model.User;
import com.foodapp.food_system.model.Product;
import com.foodapp.food_system.repository.WishlistRepository;
import com.foodapp.food_system.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private com.foodapp.food_system.repository.PaymentRepository paymentRepository;
    
    // --- LOGIN & REGISTER ---

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        if (userRepository.findByUsername(username) != null) {
            return "redirect:/register?error=Username already taken";
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole("USER");
        newUser.setWalletBalance(0.0);
        userRepository.save(newUser);

        return "redirect:/?msg=Account Created! Please Login.";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        User user = userRepository.findByUsername(username);
        
        if (user != null && user.getPassword().equals(password)) {
            // 1. SET SESSION (Crucial Step)
            session.setAttribute("username", username);
            session.setAttribute("role", user.getRole());
            
            // 2. REDIRECT (Clean URL, no "?username=")
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/dashboard";
            }
        }
        
        model.addAttribute("error", "Invalid Credentials");
        return "login";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate(); 
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        return "redirect:/?msg=LoggedOut"; 
    }
    
    // --- DASHBOARD ---

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String category,
                                Model model) {
        
        // 1. Get User from Session (Interceptor already checked if null)
        String username = (String) session.getAttribute("username");
        
        User user = userRepository.findByUsername(username);
        
        // 2. Safety Check: If DB is wiped but session exists
        if (user == null) {
            session.invalidate();
            return "redirect:/";
        }

        // 3. Send User Data
        model.addAttribute("username", username);
        model.addAttribute("role", user.getRole());
        model.addAttribute("walletBalance", user.getWalletBalance() != null ? user.getWalletBalance() : 0.0);
        model.addAttribute("userAddress", user.getAddress());

        // 4. Fetch Products
        String url = "http://localhost:8082/api/products";
        Product[] allProducts = restTemplate.getForObject(url, Product[].class);
        
        List<Product> filteredList = new ArrayList<>();
        
        if (allProducts != null) {
            for (Product p : allProducts) {
                boolean matches = true;

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
 
        model.addAttribute("products", filteredList);
        model.addAttribute("currentCategory", category != null ? category : "All");
        
        // 5. Fetch Wishlist IDs (for Heart Icons)
        java.util.List<com.foodapp.food_system.model.Wishlist> userWishlist = wishlistRepository.findByUsername(username);
        java.util.List<Long> wishlistProductIds = new ArrayList<>();
        
        if (userWishlist != null) {
            for (com.foodapp.food_system.model.Wishlist w : userWishlist) {
                wishlistProductIds.add(w.getProductId());
            }
        }
        model.addAttribute("wishlistProductIds", wishlistProductIds);

        return "dashboard";
    }
    
    // --- ADMIN SECTION ---

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username);

        // Security: Check Role
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        String url = "http://localhost:8082/api/products";
        Product[] products = restTemplate.getForObject(url, Product[].class);

        model.addAttribute("products", products);
        model.addAttribute("username", username);

        return "admin-dashboard";
    }

    @PostMapping("/admin/add-product")
    public String addProduct(HttpSession session, @ModelAttribute Product product) {
        // Just call the service
        String url = "http://localhost:8082/api/products";
        restTemplate.postForObject(url, product, Product.class);
        
        return "redirect:/admin/dashboard?msg=Action Successful";
    }

    @GetMapping("/admin/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        String url = "http://localhost:8082/api/products/" + id;
        restTemplate.delete(url);
        
        return "redirect:/admin/dashboard?msg=Item Deleted";
    }
    
    @GetMapping("/admin/orders")
    public String adminOrders(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username);

        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        try {
            String url = "http://localhost:8083/api/orders";
            List orders = restTemplate.getForObject(url, List.class);
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("error", "Order Service is Offline!");
        }

        model.addAttribute("username", username);
        return "admin-orders";
    }

    @GetMapping("/admin/order-status/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username);

        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        String url = "http://localhost:8083/api/orders/" + id + "/status?status=" + status;
        restTemplate.postForObject(url, null, Object.class);

        return "redirect:/admin/orders";
    }

    // --- ORDER & WALLET ---
    
    /*@PostMapping("/order")
    public String placeOrder(HttpSession session, 
                             @RequestParam Double price, 
                             @RequestParam String paymentMethod,
                             @RequestParam(defaultValue = "1") Integer quantity, 
                             Model model) {
        
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username);
        
        if (user == null) return "redirect:/"; // Safety check

        Double totalAmount = price * quantity;
        String orderStatus = "PAID";

        // Payment Logic
        if ("CASH".equals(paymentMethod)) {
            orderStatus = "PENDING";
        } 
        else if ("CARD".equals(paymentMethod) || "UPI".equals(paymentMethod)) {
            // External payment logic here
        } 
        else {
            // Wallet Logic
            if (user.getWalletBalance() < totalAmount) {
                return "redirect:/dashboard?error=Insufficient Wallet Balance";
            }
            user.setWalletBalance(user.getWalletBalance() - totalAmount);
            userRepository.save(user);
        }

        // Prepare Order Data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", user.getId());
        orderData.put("totalAmount", totalAmount);
        orderData.put("status", orderStatus);
        
        // Call Order Service
        try {
            String orderUrl = "http://localhost:8083/api/orders";
            restTemplate.postForObject(orderUrl, orderData, Object.class);
        } catch (Exception e) {
            System.out.println("Order Service Error: " + e.getMessage());
        }
        
        return "redirect:/dashboard?msg=Order Placed";
    }*/
    
    @PostMapping("/order")
    public String placeOrder(HttpSession session, 
                             @RequestParam Double price, 
                             @RequestParam String paymentMethod,
                             @RequestParam(defaultValue = "1") Integer quantity) {
        
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username);
        if (user == null) return "redirect:/"; 

        Double totalAmount = price * quantity;
        
        // --- 1. SET STATUS BASED ON PAYMENT METHOD ---
        String orderStatus = "PAID";      // Default for Wallet/Card
        String paymentStatus = "SUCCESS"; // Default for Wallet/Card

        String cleanPaymentMethod = paymentMethod.trim().toUpperCase();
        
        if ("CASH".equals(cleanPaymentMethod)) {
            orderStatus = "PENDING";
            paymentStatus = "PENDING";
            System.out.println("Processing COD Order...");
        } else if("UPI".equals(cleanPaymentMethod)){
        	orderStatus = "PENDING";
            paymentStatus = "PAID";
            System.out.println("Processing UPI Order...");
        }
        else if ("WALLET".equals(cleanPaymentMethod)) {
            if (user.getWalletBalance() < totalAmount) {
                return "redirect:/dashboard?error=Insufficient Wallet Balance";
            }
            // Deduct Money
            user.setWalletBalance(user.getWalletBalance() - totalAmount);
            userRepository.save(user);
        }

        // --- 2. SAVE ORDER (Call Microservice) ---
        java.util.Map<String, Object> orderData = new java.util.HashMap<>();
        orderData.put("userId", user.getId());
        orderData.put("totalAmount", totalAmount);
        orderData.put("status", orderStatus); // Sends "PENDING" or "PAID"
        Long createdOrderId = 0L; // Default if service fails
        try {
        	String orderUrl = "http://localhost:8083/api/orders";
        	java.util.Map response = restTemplate.postForObject(orderUrl, orderData, java.util.Map.class);
            
            if (response != null && response.get("id") != null) {
                // Extract the ID from the JSON response
                createdOrderId = ((Number) response.get("id")).longValue();
                System.out.println("Order Service Created Order ID: " + createdOrderId);
            }
            //String orderUrl = "http://localhost:8083/api/orders";
            //restTemplate.postForObject(orderUrl, orderData, Object.class);
        } catch (Exception e) {
            System.out.println("Order Service Error: " + e.getMessage());
        }

        // --- 3. SAVE PAYMENT RECORD (For Admin View) ---
        com.foodapp.food_system.model.Payment payment = new com.foodapp.food_system.model.Payment();
        payment.setAmount(totalAmount);
        payment.setStatus(paymentStatus); // Saves "PENDING" or "SUCCESS"
        payment.setOrderId(createdOrderId); // Now we have the REAL ID!        payment.setTimestamp(java.time.LocalDateTime.now());
       // payment.setPaymentMode(paymentMethod); // Now we save "UPI", "CASH", etc.
        payment.setTimestamp(java.time.LocalDateTime.now());
        payment.setPaymentMode(cleanPaymentMethod); // <--- SAVES "UPI", "WALLET", or "CASH"
        paymentRepository.save(payment); 
        
        // --- 4. REDIRECT WITH SUCCESS FLAG ---
        return "redirect:/dashboard?orderSuccess=true";
    }
    
    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username);

        if (user == null) {
            session.invalidate();
            return "redirect:/";
        }

        model.addAttribute("walletBalance", user.getWalletBalance());
        model.addAttribute("username", username);

        // Fetch Orders
        String url = "http://localhost:8083/api/orders/user/" + user.getId();
        try {
            java.util.List orders = restTemplate.getForObject(url, java.util.List.class);
            if (orders != null) {
                java.util.Collections.reverse(orders);
            }
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("error", "Order Service is down!");
        }

        return "my-orders";
    }

    @PostMapping("/wallet/add")
    public String addMoney(HttpSession session, @RequestParam Double amount) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username);
        
        if (user != null) {
            user.setWalletBalance(user.getWalletBalance() + amount);
            userRepository.save(user);
        }
        return "redirect:/dashboard?msg=Wallet Updated";
    }
    
//    @GetMapping("/checkout")
//    public String showCheckout(HttpSession session, Model model) {
//        // 1. Get User from Session
//        String username = (String) session.getAttribute("username");
//        if (username == null) {
//            return "redirect:/"; // Go to Login if not logged in
//        }
//
//        // 2. Fetch User Details (Address, Balance)
//        User user = userRepository.findByUsername(username);
//        
//        // 3. Send to HTML
//        model.addAttribute("user", user);
//        model.addAttribute("walletBalance", user.getWalletBalance());
//        model.addAttribute("username", username);
//        
//        return "checkout"; // This loads pay.html
//    }
}