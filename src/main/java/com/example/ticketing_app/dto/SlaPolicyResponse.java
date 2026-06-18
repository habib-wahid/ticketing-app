package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

import com.example.ticketing_app.entity.TicketPriority;

public record SlaPolicyResponse(
		String id,
		ComplaintCategorySummaryResponse category,
		TicketPriority priority,
		Integer firstResponseTimeHours,
		Integer resolutionTimeHours,
		Integer escalationAfterHours,
		Integer reminderThreshHoldHours,
		boolean active,
		String updatedBy,
		LocalDateTime updatedAt,
		LocalDateTime createdAt) {
}
