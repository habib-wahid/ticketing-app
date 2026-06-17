package com.example.ticketing_app.dto;

public record UserTicketStatsResponse(
		long totalTickets,
		long assignedTickets,
		long resolvedTickets) {
}
