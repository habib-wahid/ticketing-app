package com.example.ticketing_app.dto;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
		@Size(max = 30) String phone) {
}

