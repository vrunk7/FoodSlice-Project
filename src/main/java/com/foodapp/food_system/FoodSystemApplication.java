//package com.foodapp.food_system;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class FoodSystemApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(FoodSystemApplication.class, args);
//	}
//
//}
package com.foodapp.food_system;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FoodSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodSystemApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
