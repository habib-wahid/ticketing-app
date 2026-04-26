package com.example.ticketing_app.dto;

import jakarta.validation.constraints.Size;

public record ComplaintCategoryUpdateRequest(
		@Size(max = 100) String name) {
}

