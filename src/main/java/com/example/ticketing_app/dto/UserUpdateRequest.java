package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
		@Email @Size(max = 255) String email,
		@Size(min = 8, max = 100) String password,
		@Size(min = 1, max = 50) String firstName,
		@Size(min = 1, max = 50) String lastName,
		UserRole role,
		Boolean isActive,
		Boolean emailVerified,
		@Size(max = 50) String department,
		@Size(max = 50) String managerId) {
}

