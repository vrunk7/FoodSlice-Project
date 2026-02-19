# FoodSlice - Microservices Food Delivery Platform

FoodSlice is a robust, fault-tolerant food delivery application built using a **Microservices Architecture**. It separates user management, product inventory, and order processing into independent services to ensure scalability, data integrity, and high performance.

<img width="364" height="213" alt="Screenshot 2026-02-19 143041" src="https://github.com/user-attachments/assets/39c0e0da-b76a-4ac1-95d3-17861a1336a1" />


## Key Features

### Security & Access Control
* **Role-Based Access Control (RBAC):** Distinct dashboards for `ADMIN` (Inventory & Live Order Dispatch) and `USER` (Browsing, Cart, & Wallet).
* **Session Management:** Implemented `HttpSession` with a custom `HandlerInterceptor` to secure routes and prevent unauthorized access without relying on heavy security frameworks.

### Transaction Safety (Distributed Systems)
* **Smart Wallet System:** Users can load funds and pay via a digital wallet, UPI, or Cash on Delivery.
* **Manual Rollback Mechanism:** Built a fault-tolerant transaction flow. If the Order Microservice crashes during checkout, the User Service automatically catches the exception and **refunds the user's wallet**, ensuring zero data inconsistency.

### Hybrid State Management
* **Zero-Latency Cart:** The shopping cart is managed entirely on the client-side using browser `localStorage`. This eliminates unnecessary server calls and database hits until the final checkout.

### Live Order Lifecycle
* **Real-Time Status Tracking:** Admin can move orders through a logical flow (`PAID` ➡️ `COOKING` ➡️ `DELIVERED`), which instantly reflects on the User's dashboard.
* **Dynamic Invoicing:** JavaScript-powered instant receipt generation for completed orders.

---

## Tech Stack

* **Backend:** Java 17, Spring Boot, Spring Web
* **Inter-Service Communication:** REST APIs, `RestTemplate`
* **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript, Bootstrap 5
* **Database:** MySQL, Spring Data JPA / Hibernate
* **Architecture:** Microservices (3 Independent Services)

---

## Microservices Breakdown

The application is split into three standalone Spring Boot applications running on different ports:

1. **User Interface & Gateway (Port `8081`)**
   * Acts as the main entry point and renders Thymeleaf templates.
   * Manages User Authentication, Sessions, and Wallet Balances.
   * Communicates with other services to aggregate data for the frontend.

2. **Product & Inventory Service (Port `8082`)**
   * Manages the food menu database.
   * Exposes REST APIs to fetch, add, or delete food items.

3. **Order & Transaction Service (Port `8083`)**
   * Handles the creation and lifecycle of user orders.
   * Stores transaction history independently from user data.

4. **Payment Service (Port `8084`)**
   * Acts as the financial ledger for the application.
   * securely tracks transaction history, payment modes (CASH, WALLET, UPI), and payment statuses (`PENDING`, `SUCCESS`).
   * Decouples financial record-keeping from the main user database.
---

## How to Run Locally

### Prerequisites
* Java Development Kit (JDK) 17+
* MySQL Server running on default port `3306`
* Maven

### Setup Steps
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/vrunk7/FoodSlice.git](https://github.com/vrunk7/FoodSlice.git)
   Database Setup:
2. **Database Setup:**
* Create four separate databases in MySQL: `food_users`, `food_products`, `food_orders`, and `food_payments`.
* Update the application.properties in each microservice with your MySQL username and password.

3. **Run the Microservices:**
* Start the Product Service (Port 8082).
* Start the Order Service (Port 8083).
* Start the Payment Service (Port 8084).
* Start the User/Frontend Service (Port 8081).

4. **Access the Application:**
Open your browser and navigate to: http://localhost:8081

### Developer Notes
This project was developed to demonstrate practical knowledge of distributed systems, RESTful API communication, and frontend-backend state synchronization. The primary focus was placed on transaction safety (rollbacks) across independent databases.
