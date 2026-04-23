package com.example.ticketing_app.dto;

import java.util.List;
import java.util.Map;

import com.example.ticketing_app.entity.TicketCategory;
import com.example.ticketing_app.entity.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
		@NotBlank @Size(min = 5, max = 200) String title,
		@NotBlank @Size(min = 10, max = 5000) String description,
		@NotNull TicketCategory category,
		@NotNull TicketPriority priority,
		String createdByUserId,
		String assignedToUserId,
		@Size(max = 5) List<@Size(max = 20) String> tags,
		Map<String, Object> customFields) {
}

