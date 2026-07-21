package com.example.ticketing_app.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "tickets")
@CompoundIndexes({
		// Serves "my created tickets" (prefix) and "my resolved tickets" (createdBy.userId + status)
		@CompoundIndex(name = "idx_createdBy_status", def = "{'createdBy.userId': 1, 'status': 1}"),
		// Serves "tickets assigned to me"
		@CompoundIndex(name = "idx_assignedTo_userId", def = "{'assignedTo.userId': 1}")
})
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
	private ComplaintCategory category;
	private TicketPriority priority;
	private TicketStatus status = TicketStatus.NEW;
	private TicketCreatedBy createdBy;
	private TicketAssignedTo assignedTo;
	private TicketDistributedBy distributedBy;
	private LocalDateTime assignedAt;
	private LocalDateTime resolvedAt;
	private LocalDateTime closedAt;
	private String slaPolicyId;
	private LocalDateTime slaDeadline;
	private LocalDateTime responseDeadline;
	private LocalDateTime escalationDueAt;
	private LocalDateTime nextReminderAt;
	private LocalDateTime slaBreachedAt;
	private Integer escalationLevel = 0;
	private Long firstResponseMinutes;
	private Boolean responseBreached;
	private LocalDateTime pausedAt;
	private long pausedMinutesTotal;
	private List<TicketComment> comments = new ArrayList<>();
	private List<TicketAttachment> attachments = new ArrayList<>();
	private List<TicketStatusHistory> statusHistory = new ArrayList<>();
	private List<TicketAssignmentHistory> assignmentHistory = new ArrayList<>();
	private List<TicketSlaEvent> slaEvents = new ArrayList<>();
	private List<String> tags = new ArrayList<>();
	private Map<String, Object> customFields = new HashMap<>();
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
