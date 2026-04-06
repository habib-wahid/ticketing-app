package com.example.ticketing_app.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing_app.dto.TicketCommentCreateRequest;
import com.example.ticketing_app.dto.TicketCommentDeleteRequest;
import com.example.ticketing_app.dto.TicketCommentResponse;
import com.example.ticketing_app.dto.TicketCommentUpdateRequest;
import com.example.ticketing_app.dto.TicketCreateRequest;
import com.example.ticketing_app.dto.TicketResponse;
import com.example.ticketing_app.dto.TicketSummaryResponse;
import com.example.ticketing_app.dto.TicketUpdateRequest;
import com.example.ticketing_app.service.TicketService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets")
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@GetMapping
	public List<TicketSummaryResponse> findAll() {
		return ticketService.findAll();
	}

	@GetMapping("/{ticketId}")
	public TicketResponse findByTicketId(@PathVariable String ticketId) {
		return ticketService.findByTicketId(ticketId);
	}

	@GetMapping("/{ticketId}/comments")
	public List<TicketCommentResponse> findComments(@PathVariable String ticketId) {
		return ticketService.findComments(ticketId);
	}

	@PostMapping
	public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
	}

	@PostMapping("/{ticketId}/comments")
	public ResponseEntity<TicketCommentResponse> addComment(@PathVariable String ticketId,
			@Valid @RequestBody TicketCommentCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addComment(ticketId, request));
	}

	@PutMapping("/{ticketId}")
	public TicketResponse update(@PathVariable String ticketId, @Valid @RequestBody TicketUpdateRequest request) {
		return ticketService.update(ticketId, request);
	}

	@PutMapping("/{ticketId}/comments/{commentId}")
	public TicketCommentResponse updateComment(@PathVariable String ticketId, @PathVariable String commentId,
			@Valid @RequestBody TicketCommentUpdateRequest request) {
		return ticketService.updateComment(ticketId, commentId, request);
	}

	@DeleteMapping("/{ticketId}")
	public ResponseEntity<Void> delete(@PathVariable String ticketId) {
		ticketService.delete(ticketId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{ticketId}/comments/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable String ticketId, @PathVariable String commentId,
			@Valid @RequestBody TicketCommentDeleteRequest request) {
		ticketService.deleteComment(ticketId, commentId, request);
		return ResponseEntity.noContent().build();
	}
}
