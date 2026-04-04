package com.example.ticketing_app.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.ticketing_app.dto.UserCreateRequest;
import com.example.ticketing_app.dto.UserResponse;
import com.example.ticketing_app.dto.UserUpdateRequest;
import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.exception.ConflictException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<UserResponse> findAll() {
		return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	public UserResponse findByUserId(String userId) {
		return toResponse(getUserEntity(userId));
	}

	public UserResponse create(UserCreateRequest request) {
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
		user.setRole(request.role());
		user.setActive(request.isActive() == null || request.isActive());
		user.setEmailVerified(request.emailVerified() != null && request.emailVerified());
		user.setDepartment(normalize(request.department()));
		user.setManagerId(normalize(request.managerId()));
		user.setPreferences(new HashMap<>());
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());

		return toResponse(userRepository.save(user));
	}

	public UserResponse update(String userId, UserUpdateRequest request) {
		User user = getUserEntity(userId);

		if (StringUtils.hasText(request.email())) {
			String normalizedEmail = request.email().trim().toLowerCase();
			userRepository.findByEmail(normalizedEmail)
					.filter(existing -> !existing.getUserId().equals(userId))
					.ifPresent(existing -> {
						throw new ConflictException("Email already exists: " + normalizedEmail);
					});
			user.setEmail(normalizedEmail);
		}
		if (StringUtils.hasText(request.password())) {
			user.setPasswordHash(passwordEncoder.encode(request.password()));
		}
		if (StringUtils.hasText(request.firstName())) {
			user.setFirstName(request.firstName().trim());
		}
		if (StringUtils.hasText(request.lastName())) {
			user.setLastName(request.lastName().trim());
		}
		if (request.role() != null) {
			user.setRole(request.role());
		}
		if (request.isActive() != null) {
			user.setActive(request.isActive());
		}
		if (request.emailVerified() != null) {
			user.setEmailVerified(request.emailVerified());
		}
		if (request.department() != null) {
			user.setDepartment(normalize(request.department()));
		}
		if (request.managerId() != null) {
			user.setManagerId(normalize(request.managerId()));
		}

		user.setUpdatedAt(LocalDateTime.now());
		return toResponse(userRepository.save(user));
	}

	public void delete(String userId) {
		User user = getUserEntity(userId);
		userRepository.delete(user);
	}

	public User requireExistingUser(String userId) {
		return getUserEntity(userId);
	}

	private User getUserEntity(String userId) {
		return userRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
				user.getUserId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getRole(),
				user.isActive(),
				user.isEmailVerified(),
				user.getDepartment(),
				user.getManagerId(),
				user.getLastLoginAt(),
				user.getCreatedAt(),
				user.getUpdatedAt(),
				user.getPreferences());
	}

	private String generateUserId() {
		String userId;
		do {
			userId = "usr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		} while (userRepository.existsByUserId(userId));
		return userId;
	}

	private String normalize(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}

