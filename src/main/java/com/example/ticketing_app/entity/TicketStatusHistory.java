package com.example.ticketing_app.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketStatusHistory {

	private TicketStatus fromStatus;
	private TicketStatus toStatus;
	private String changedBy;
	private LocalDateTime changedAt;
	private String reason;
}

