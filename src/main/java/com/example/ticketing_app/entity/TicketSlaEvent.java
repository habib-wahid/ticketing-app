package com.example.ticketing_app.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketSlaEvent {

	private TicketSlaEventType eventType;
	private LocalDateTime triggeredAt;
	private List<UserRole> notifiedRoles = new ArrayList<>();
}

