package com.example.ticketing_app.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ticketing_app.dto.AuthLoginRequest;
import com.example.ticketing_app.dto.AuthRegisterRequest;
import com.example.ticketing_app.dto.AuthResponse;
import com.example.ticketing_app.dto.AuthUserResponse;
import com.example.ticketing_app.dto.RefreshTokenRequest;
import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.entity.UserRole;
import com.example.ticketing_app.exception.ConflictException;
import com.example.ticketing_app.exception.UnauthorizedException;
import com.example.ticketing_app.repository.UserRepository;
import com.example.ticketing_app.security.JwtTokenProvider;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;

	public AuthService(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider tokenProvider,
			RefreshTokenService refreshTokenService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenProvider = tokenProvider;
		this.refreshTokenService = refreshTokenService;
	}

	public AuthResponse register(AuthRegisterRequest request) {
		String normalizedEmail = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(normalizedEmail)) {
			throw new ConflictException("Email already exists: " + normalizedEmail);
		}

		User user = new User();
		user.setUserId(generateUserId());
		user.setEmail(normalizedEmail);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setFirstName(request.firstName().trim());
		user.setLastName(request.lastName().trim());
		user.setRole(UserRole.CUSTOMER);
		user.setActive(true);
		user.setEmailVerified(false);
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());

		User savedUser = userRepository.save(user);
		return buildAuthResponse(savedUser);
	}

	public AuthResponse login(AuthLoginRequest request) {
		String normalizedEmail = request.email().trim().toLowerCase();
		User user = userRepository.findByEmail(normalizedEmail)
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
		if (!user.isActive()) {
			throw new UnauthorizedException("User is inactive");
		}
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new UnauthorizedException("Invalid credentials");
		}
		user.setLastLoginAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
		userRepository.save(user);
		return buildAuthResponse(user);
	}

	public AuthResponse refresh(RefreshTokenRequest request) {
		var refreshToken = refreshTokenService.validate(request.refreshToken());
		User user = userRepository.findByUserId(refreshToken.getUserId())
				.orElseThrow(() -> new UnauthorizedException("User not found"));
		refreshTokenService.revoke(refreshToken);
		return buildAuthResponse(user);
	}

	private AuthResponse buildAuthResponse(User user) {
		String accessToken = tokenProvider.generateAccessToken(user);
		String refreshToken = refreshTokenService.createToken(user);
		return new AuthResponse(
				accessToken,
				refreshToken,
				"Bearer",
				tokenProvider.getAccessTokenExpirySeconds(),
				new AuthUserResponse(user.getUserId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole()));
	}

	private String generateUserId() {
		String userId;
		do {
			userId = "usr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		} while (userRepository.existsByUserId(userId));
		return userId;
	}
}

