package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.UserRole;

public record AuthUserResponse(
		String userId,
		String email,
		String firstName,
		String lastName,
		UserRole role) {
}

