package com.example.ticketing_app.service.sla;

import java.time.LocalDateTime;

public record SlaDeadlines(
		LocalDateTime responseDeadline,
		LocalDateTime slaDeadline,
		LocalDateTime escalationDueAt,
		LocalDateTime nextReminderAt) {
}
