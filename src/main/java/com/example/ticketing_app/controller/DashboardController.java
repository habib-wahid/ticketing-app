package com.example.ticketing_app.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ticketing_app.repository.TicketCategoryCountProjection;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing_app.dto.ApiResponse;
import com.example.ticketing_app.dto.TicketDashboardResponse;
import com.example.ticketing_app.dto.TicketPriorityDashboardResponse;
import com.example.ticketing_app.dto.TicketComplaintCategoryDashboardResponse;
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
	public ResponseEntity<ApiResponse<TicketDashboardResponse>> getTicketCountInDashboard(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		return ResponseEntity.ok(ApiResponse.success("Ticket dashboard fetched",
				ticketService.getDashboardCounts(actor(principal), from, to)));
	}

	@GetMapping("/tickets-by-priority")
	public ResponseEntity<ApiResponse<TicketPriorityDashboardResponse>> getTicketCountByPriority(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		return ResponseEntity.ok(ApiResponse.success("Ticket priority dashboard fetched",
				ticketService.getDashboardCountsByPriority(actor(principal), from, to)));
	}

	@GetMapping("/tickets-by-complaint-category")
	public ResponseEntity<ApiResponse<List<TicketCategoryCountProjection>>> getTicketCountByComplaintCategory(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		return ResponseEntity.ok(ApiResponse.success("Ticket complaint category dashboard fetched",
				ticketService.getDashboardCountsByComplaintCategory(actor(principal), from, to)));
	}

	private ActorContext actor(UserPrincipal principal) {
		return new ActorContext(principal.getUserId(), principal.getRole());
	}
}
