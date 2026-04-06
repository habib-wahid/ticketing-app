package com.example.ticketing_app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TicketCommentResponse(
		String commentId,
		TicketAuthorResponse author,
		String text,
		boolean internal,
		List<String> attachments,
		LocalDateTime createdAt) {
}

