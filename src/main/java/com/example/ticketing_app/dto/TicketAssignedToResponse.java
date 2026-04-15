package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.UserRole;

public record TicketAssignedToResponse(
		String userId,
		String name,
		UserRole role) {
}

