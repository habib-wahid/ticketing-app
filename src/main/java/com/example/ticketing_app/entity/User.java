package com.example.ticketing_app.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

	@Id
	private String id;

	@Indexed(unique = true)
	private String userId;

	@Indexed(unique = true)
	private String email;

	private String passwordHash;
	private String firstName;
	private String lastName;
	private UserRole role;
	private boolean isActive = true;
	private boolean emailVerified = false;
	private String department;
	private String managerId;
	private LocalDateTime lastLoginAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Map<String, Object> preferences = new HashMap<>();
}

