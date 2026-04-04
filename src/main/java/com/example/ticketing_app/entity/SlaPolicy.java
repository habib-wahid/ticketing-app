package com.example.ticketing_app.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "sla_policies")
@Getter
@Setter
@NoArgsConstructor
public class SlaPolicy {

	@Id
	private String id;

	@Indexed(unique = true)
	private TicketPriority priority;

	private Integer responseTimeHours;
	private Integer resolutionTimeHours;
	private Integer escalationAfterHours;
	private Integer reminderIntervalMinutes;
	private boolean active = true;
	private String updatedBy;
	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;

}

