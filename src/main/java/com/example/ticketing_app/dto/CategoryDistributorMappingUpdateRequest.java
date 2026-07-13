package com.example.ticketing_app.dto;

import jakarta.validation.constraints.Size;

public record CategoryDistributorMappingUpdateRequest(
		@Size(max = 100) String distributorUserId,
		Boolean active) {
}
