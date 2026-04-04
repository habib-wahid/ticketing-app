package com.example.ticketing_app.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.example.ticketing_app.entity.UserRole;

public record UserResponse(
		String userId,
		String email,
		String firstName,
		String lastName,
		UserRole role,
		boolean isActive,
		boolean emailVerified,
		String department,
		String managerId,
		LocalDateTime lastLoginAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		Map<String, Object> preferences) {
}

