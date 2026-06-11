package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;

public record TicketSearchRequest(
		String title,
		String categoryId,
		TicketPriority priority,
		TicketStatus status,
		String createdBy,
		String assignedTo,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
}
