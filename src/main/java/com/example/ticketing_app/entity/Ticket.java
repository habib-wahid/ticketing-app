package com.example.ticketing_app.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

	@Id
	private String id;

	@Indexed(unique = true)
	private String ticketId;

	private String title;
	private String description;
	private TicketCategory category;
	private TicketPriority priority;
	private TicketStatus status = TicketStatus.NEW;
	private String createdByUserId;
	private String assignedToUserId;
	private LocalDateTime assignedAt;
	private LocalDateTime resolvedAt;
	private LocalDateTime closedAt;
	private LocalDateTime slaDeadline;
	private LocalDateTime responseDeadline;
	private LocalDateTime escalationDueAt;
	private LocalDateTime nextReminderAt;
	private LocalDateTime slaBreachedAt;
	private Integer escalationLevel = 0;
	private List<String> tags = new ArrayList<>();
	private Map<String, Object> customFields = new HashMap<>();
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
