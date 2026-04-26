package com.example.ticketing_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintCategoryCreateRequest(
		@NotBlank @Size(max = 100) String name) {
}

