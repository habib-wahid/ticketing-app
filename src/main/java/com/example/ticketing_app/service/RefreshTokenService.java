package com.example.ticketing_app.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.ticketing_app.entity.RefreshToken;
import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.exception.UnauthorizedException;
import com.example.ticketing_app.repository.RefreshTokenRepository;
import com.example.ticketing_app.security.JwtProperties;

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProperties jwtProperties;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtProperties = jwtProperties;
	}

	public String createToken(User user) {
		String rawToken = UUID.randomUUID().toString().replace("-", "");
		String hash = hashToken(rawToken);
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUserId(user.getUserId());
		refreshToken.setTokenHash(hash);
		refreshToken.setCreatedAt(Instant.now());
		refreshToken.setExpiresAt(Instant.now().plus(jwtProperties.refreshTokenDuration()));
		refreshTokenRepository.save(refreshToken);
		return rawToken;
	}

	public RefreshToken validate(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new UnauthorizedException("Refresh token is required");
		}
		String hash = hashToken(rawToken);
		RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
		if (token.getRevokedAt() != null) {
			throw new UnauthorizedException("Refresh token revoked");
		}
		if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(Instant.now())) {
			revoke(token);
			throw new UnauthorizedException("Refresh token expired");
		}
		return token;
	}

	public void revoke(RefreshToken token) {
		token.setRevokedAt(Instant.now());
		refreshTokenRepository.save(token);
	}

	private String hashToken(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder();
			for (byte b : hashed) {
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 not available", ex);
		}
	}
}

