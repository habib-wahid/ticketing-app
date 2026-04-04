package com.example.ticketing_app.dto;

import java.util.List;
import java.util.Map;

import com.example.ticketing_app.entity.TicketCategory;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;

import jakarta.validation.constraints.Size;

public record TicketUpdateRequest(
		@Size(min = 5, max = 200) String title,
		@Size(min = 10, max = 5000) String description,
		TicketCategory category,
		TicketPriority priority,
		TicketStatus status,
		String assignedToUserId,
		List<@Size(max = 20) String> tags,
		Map<String, Object> customFields) {
}

