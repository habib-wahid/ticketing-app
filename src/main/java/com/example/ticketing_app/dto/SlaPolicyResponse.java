package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

import com.example.ticketing_app.entity.TicketPriority;

public record SlaPolicyResponse(
		String id,
		TicketPriority priority,
		Integer responseTimeHours,
		Integer resolutionTimeHours,
		Integer escalationAfterHours,
		Integer reminderIntervalMinutes,
		boolean active,
		String updatedBy,
		LocalDateTime updatedAt,
		LocalDateTime createdAt) {
}

