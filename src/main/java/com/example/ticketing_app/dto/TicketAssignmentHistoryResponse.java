package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

import com.example.ticketing_app.entity.TicketAssignmentAction;
import com.example.ticketing_app.entity.UserRole;

public record TicketAssignmentHistoryResponse(
		String userId,
		String name,
		UserRole role,
		LocalDateTime fromAt,
		LocalDateTime toAt,
		Long durationMinutes,
		TicketAssignmentAction action,
		String actedByUserId,
		String actedByName,
		String reason,
		String distributedByUserId) {
}
