package com.example.ticketing_app.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketAssignmentHistory {

	private String userId;
	private String name;
	private UserRole role;
	private LocalDateTime fromAt;
	private LocalDateTime toAt;
	private Long durationMinutes;
	private TicketAssignmentAction action;
	private String actedByUserId;
	private String actedByName;
	private String reason;
	private String distributedByUserId;
}
