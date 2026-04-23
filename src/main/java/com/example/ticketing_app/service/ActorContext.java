package com.example.ticketing_app.service;

import com.example.ticketing_app.entity.UserRole;

public record ActorContext(String userId, UserRole role) {
	public boolean isAdmin() {
		return role == UserRole.ADMIN;
	}
}

