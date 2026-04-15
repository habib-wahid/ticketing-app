package com.example.ticketing_app.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.ticketing_app.entity.TicketCategory;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;

public record TicketResponse(
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
		List<TicketCommentResponse> comments,
		List<TicketAttachmentResponse> attachments,
		List<TicketStatusHistoryResponse> statusHistory,
		List<TicketSlaEventResponse> slaEvents,
		List<String> tags,
		Map<String, Object> customFields,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
