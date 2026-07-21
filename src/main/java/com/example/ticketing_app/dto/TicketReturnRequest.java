package com.example.ticketing_app.dto;

import jakarta.validation.constraints.Size;

public record TicketReturnRequest(
		@Size(max = 255) String reason) {
}
