//package com.foodapp.food_system.controller; // Change package if needed

package com.foodapp.food_system.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.foodapp.food_system.model.User;
import com.foodapp.food_system.repository.UserRepository;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
    	System.out.println(">>> INTERCEPTOR HIT: " + request.getRequestURI());

        HttpSession session = request.getSession(false);

        // 1️⃣ Not logged in
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("/");
            return false;
        }

        String username = (String) session.getAttribute("username");

        // 2️⃣ Role protection for admin URLs
        if (request.getRequestURI().startsWith("/admin")) {

            User user = userRepository.findByUsername(username);

            if (user == null || !"ADMIN".equals(user.getRole())) {
                response.sendRedirect("/dashboard");
                return false;
            }
        }

        // 3️⃣ Prevent back button caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        return true;
    }
}



//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//@Component
//public class SessionInterceptor implements HandlerInterceptor {
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // 1. Get the Session
//        HttpSession session = request.getSession();
//
//        // 2. Check if "username" exists in the session
//        if (session.getAttribute("username") == null) {
//            // If NOT logged in, kick them to the login page
//            response.sendRedirect("/"); 
//            return false; // Stop the request right here
//        }
//
//        // 3. THE CACHE BUSTER (Crucial for Back Button security)
//        // This tells the browser: "Never save this page. Always ask the server."
//        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
//        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
//        response.setDateHeader("Expires", 0); // Proxies
//
//        return true; // Allow the request to proceed
//    }
//}