package com.example.ticketing_app.dto;

public record TicketDailyStatusResponse(
		String date,
		long reportedCount,
		long solvedCount) {
}

