package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

public record CategoryDistributorMappingResponse(
		String id,
		String categoryId,
		String categoryName,
		String distributorUserId,
		String distributorName,
		boolean active,
		String createdBy,
		LocalDateTime createdAt,
		String updatedBy,
		LocalDateTime updatedAt) {
}
