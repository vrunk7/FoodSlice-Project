package com.foodapp.food_system.controller;

import com.foodapp.food_system.model.Order;
import com.foodapp.food_system.repository.OrderRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

   /* @Autowired
    private OrderRepository orderRepository;

    // 1. Place a new Order
    @PostMapping
    public Order placeOrder(@RequestBody Order order) {
        order.setStatus("PLACED");
        order.setOrderDate(java.time.LocalDateTime.now());
        return orderRepository.save(order);
    }
*/
    // 2. Get Order History (Optional, but looks good)
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderRepository.findById(id).orElse(null);
    }

        @Autowired
        private OrderRepository orderRepository;

        // We can't use @Autowired for RestTemplate here easily because we didn't create a Bean in this Service instance.
        // Hack for 48-hour deadline: Create a new RestTemplate manually.
        private RestTemplate restTemplate = new RestTemplate(); 

        @PostMapping
        public Order placeOrder(@RequestBody Order order) {
            // 1. Save the Order first
            order.setStatus("PLACED");
            order.setOrderDate(java.time.LocalDateTime.now());
            Order savedOrder = orderRepository.save(order);

            // 2. CALL PAYMENT SERVICE (Port 8084) automatically
            // Create the payment data
            java.util.Map<String, Object> paymentData = new java.util.HashMap<>();
            paymentData.put("orderId", savedOrder.getId());
            paymentData.put("amount", savedOrder.getTotalAmount());
            
            try {
                String paymentUrl = "http://localhost:8084/api/payments";
                restTemplate.postForObject(paymentUrl, paymentData, Object.class);
                savedOrder.setStatus("PAID"); // Update status if payment worked
            } catch (Exception e) {
                System.out.println("Payment Service is down! Order saved as PLACED.");
            }

            // 3. Update the order with new status
            return orderRepository.save(savedOrder);
        }
        
        @GetMapping("/user/{userId}")
        public java.util.List<Order> getUserOrders(@PathVariable Long userId) {
            return orderRepository.findByUserId(userId);
        }
        
        
        // 1. ADMIN: Get ALL Orders (for the dashboard)
        @GetMapping
        public List<Order> getAllOrders() {
            // This finds every single order in the database
            // Sort by ID descending so newest orders show first!
            return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }

        // 2. ADMIN: Update Order Status (Cooking -> Delivered)
        @PostMapping("/{id}/status")
        public Order updateStatus(@PathVariable Long id, @RequestParam String status) {
            Order order = orderRepository.findById(id).orElse(null);
            if (order != null) {
                order.setStatus(status);
                return orderRepository.save(order);
            }
            return null;
        }
    }
