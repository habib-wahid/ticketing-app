package com.example.ticketing_app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SlaPolicyUpdateRequest(
		@Min(1) Integer responseTimeHours,
		@Min(1) Integer resolutionTimeHours,
		@Min(1) Integer escalationAfterHours,
		@Min(1) Integer reminderIntervalMinutes,
		Boolean active,
		@Size(max = 100) String updatedBy) {
}

