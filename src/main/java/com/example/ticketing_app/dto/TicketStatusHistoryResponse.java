package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

import com.example.ticketing_app.entity.TicketStatus;

public record TicketStatusHistoryResponse(
		TicketStatus fromStatus,
		TicketStatus toStatus,
		String changedBy,
		LocalDateTime changedAt,
		String reason) {
}

