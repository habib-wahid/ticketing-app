package com.example.ticketing_app.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCommentUpdateRequest(
		@NotBlank String updatedByUserId,
		@Size(min = 1, max = 5000) String text,
		Boolean internal,
		List<String> attachments) {
}

