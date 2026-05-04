package com.example.ticketing_app.dto;

public record TicketDashboardResponse(
		long openTickets,
		long newTickets,
		long inProcessTickets,
		long closedTickets) {
}

