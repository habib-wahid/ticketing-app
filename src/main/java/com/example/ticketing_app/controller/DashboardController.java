package com.example.ticketing_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing_app.dto.ApiResponse;
import com.example.ticketing_app.dto.TicketDashboardResponse;
import com.example.ticketing_app.security.UserPrincipal;
import com.example.ticketing_app.service.ActorContext;
import com.example.ticketing_app.service.TicketService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

	private final TicketService ticketService;

	public DashboardController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@GetMapping("/tickets")
	public ResponseEntity<ApiResponse<TicketDashboardResponse>> getTicketDashboard(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Ticket dashboard fetched",
				ticketService.getDashboardCounts(actor(principal))));
	}

	private ActorContext actor(UserPrincipal principal) {
		return new ActorContext(principal.getUserId(), principal.getRole());
	}
}

