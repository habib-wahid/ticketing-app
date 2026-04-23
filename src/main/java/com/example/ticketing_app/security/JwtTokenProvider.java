package com.example.ticketing_app.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.entity.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private static final String ROLE_CLAIM = "role";
	private static final String EMAIL_CLAIM = "email";

	private final JwtProperties properties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties properties) {
		this.properties = properties;
		this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plus(properties.accessTokenDuration());
		return Jwts.builder()
				.subject(user.getUserId())
				.issuer(properties.getIssuer())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.claim(EMAIL_CLAIM, user.getEmail())
				.claim(ROLE_CLAIM, user.getRole().name())
				.signWith(secretKey)
				.compact();
	}

	public boolean isValid(String token) {
		parseClaims(token);
		return true;
	}

	public String getUserId(String token) {
		return parseClaims(token).getPayload().getSubject();
	}

	public String getEmail(String token) {
		return parseClaims(token).getPayload().get(EMAIL_CLAIM, String.class);
	}

	public UserRole getRole(String token) {
		String role = parseClaims(token).getPayload().get(ROLE_CLAIM, String.class);
		return role == null ? null : UserRole.valueOf(role);
	}

	public long getAccessTokenExpirySeconds() {
		return properties.accessTokenDuration().toSeconds();
	}

	private Jws<Claims> parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.requireIssuer(properties.getIssuer())
				.build()
				.parseSignedClaims(token);
	}
}

