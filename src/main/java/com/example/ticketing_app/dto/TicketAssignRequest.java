package com.example.ticketing_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.example.ticketing_app.entity.TicketStatus;

public record TicketAssignRequest(
		@NotBlank String assignedToUserId,
		@NotBlank String assignedByUserId,
		@Size(max = 255) String reason) {
}
