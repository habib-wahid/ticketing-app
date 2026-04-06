package com.example.ticketing_app.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ticketing_app.entity.TicketSlaEventType;
import com.example.ticketing_app.entity.UserRole;

public record TicketSlaEventResponse(
		TicketSlaEventType eventType,
		LocalDateTime triggeredAt,
		List<UserRole> notifiedRoles) {
}

