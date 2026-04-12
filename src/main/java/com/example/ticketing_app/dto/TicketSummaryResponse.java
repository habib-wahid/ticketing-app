package com.example.ticketing_app.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.ticketing_app.entity.TicketCategory;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;

public record TicketSummaryResponse(
		String ticketId,
		String title,
		String description,
		TicketCategory category,
		TicketPriority priority,
		TicketStatus status,
		TicketCreatedByResponse createdBy,
		String assignedToUser,
		LocalDateTime assignedAt,
		LocalDateTime resolvedAt,
		LocalDateTime closedAt,
		TicketSlaSummary sla,
		LocalDateTime responseDeadline,
		LocalDateTime escalationDueAt,
		LocalDateTime nextReminderAt,
		LocalDateTime slaBreachedAt,
		Integer escalationLevel,
		List<String> tags,
		Map<String, Object> customFields,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
