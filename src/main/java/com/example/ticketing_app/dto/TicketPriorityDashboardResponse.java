package com.example.ticketing_app.dto;

public record TicketPriorityDashboardResponse(
		long low,
		long medium,
		long high,
		long critical) {
}

