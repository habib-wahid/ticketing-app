package com.example.ticketing_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketAssignRequest(
		@NotBlank String assignedToUserId,
		@Size(max = 100) String assignedByUserId,
		@Size(max = 255) String reason) {
}
