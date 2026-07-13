package com.example.ticketing_app.service;

import com.example.ticketing_app.entity.UserRole;

public record ActorContext(String userId, UserRole role) {
	public boolean isAdmin() {
		return role == UserRole.ADMIN;
	}

    public boolean isAgent() {
        return role == UserRole.AGENT;
    }

    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

	public boolean isDistributor() {
		return role == UserRole.DISTRIBUTOR;
	}
}

