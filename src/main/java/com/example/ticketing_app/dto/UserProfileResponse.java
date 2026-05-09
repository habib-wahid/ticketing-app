package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.UserRole;

public record UserProfileResponse(
		String fullName,
		String email,
		String phone,
		UserRole role) {
}

