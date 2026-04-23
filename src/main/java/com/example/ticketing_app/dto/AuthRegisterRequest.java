package com.example.ticketing_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8, max = 128) String password,
		@NotBlank @Size(max = 60) String firstName,
		@NotBlank @Size(max = 60) String lastName) {
}

