package com.example.ticketing_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDistributorMappingCreateRequest(
		@NotBlank @Size(max = 100) String categoryId,
		@NotBlank @Size(max = 100) String distributorUserId,
		Boolean active) {
}
