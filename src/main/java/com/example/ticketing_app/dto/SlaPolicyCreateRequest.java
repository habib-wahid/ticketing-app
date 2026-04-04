package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.TicketPriority;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SlaPolicyCreateRequest(
		@NotNull TicketPriority priority,
		@NotNull @Min(1) Integer responseTimeHours,
		@NotNull @Min(1) Integer resolutionTimeHours,
		@NotNull @Min(1) Integer escalationAfterHours,
		@NotNull @Min(1) Integer reminderIntervalMinutes,
		Boolean active,
		@Size(max = 100) String updatedBy) {
}

