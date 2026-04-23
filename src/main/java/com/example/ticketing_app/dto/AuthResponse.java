package com.example.ticketing_app.dto;

public record AuthResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresInSeconds,
		AuthUserResponse user) {
}

