package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotBlank @Size(min = 1, max = 50) String firstName,
		@NotBlank @Size(min = 1, max = 50) String lastName,
		@NotNull UserRole role,
		Boolean isActive,
		Boolean emailVerified,
		@Size(max = 50) String department,
		@Size(max = 50) String managerId) {
}

