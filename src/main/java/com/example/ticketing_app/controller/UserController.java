package com.example.ticketing_app.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing_app.dto.UserCreateRequest;
import com.example.ticketing_app.dto.UserResponse;
import com.example.ticketing_app.dto.UserUpdateRequest;
import com.example.ticketing_app.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<UserResponse> findAll() {
		return userService.findAll();
	}

	@GetMapping("/{userId}")
	public UserResponse findByUserId(@PathVariable String userId) {
		return userService.findByUserId(userId);
	}

	@PostMapping
	public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
	}

	@PutMapping("/{userId}")
	public UserResponse update(@PathVariable String userId, @Valid @RequestBody UserUpdateRequest request) {
		return userService.update(userId, request);
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> delete(@PathVariable String userId) {
		userService.delete(userId);
		return ResponseEntity.noContent().build();
	}
}
