package com.example.ticketing_app.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
		@NotBlank String refreshToken) {
}

