package com.foodapp.food_system.controller;

import com.foodapp.food_system.model.Payment;
import com.foodapp.food_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping
    public Payment makePayment(@RequestBody Payment payment) {
        payment.setStatus("SUCCESS"); // We always pretend it works!
        payment.setTimestamp(java.time.LocalDateTime.now());
        return paymentRepository.save(payment);
    }
}