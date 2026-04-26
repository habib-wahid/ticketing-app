package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

public record ComplaintCategoryResponse(
		String id,
		String name,
		String createdBy,
		LocalDateTime createdAt,
		String updatedBy,
		LocalDateTime updatedAt) {
}

