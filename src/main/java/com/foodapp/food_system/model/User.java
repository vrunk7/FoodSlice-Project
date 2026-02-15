package com.foodapp.food_system.model;

import jakarta.persistence.*;
import lombok.Data; // Lombok automatically creates Getters/Setters

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;
	private String address;
	private Double walletBalance;
	
	
	public Double getWalletBalance() { 
		return walletBalance; 
	}
	
	public void setWalletBalance(Double walletBalance) {
		this.walletBalance = walletBalance; 
	}
    
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	public String getAddress() { 
		return address; 
	}
	public void setAddress(String address) { 
		this.address = address; 
	}
}