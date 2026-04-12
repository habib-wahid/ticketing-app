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

import com.example.ticketing_app.dto.ApiResponse;
import com.example.ticketing_app.dto.TicketAssignRequest;
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
	public ResponseEntity<ApiResponse<List<TicketSummaryResponse>>> findAll() {
		return ResponseEntity.ok(ApiResponse.success("Tickets fetched", ticketService.findAll()));
	}

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TicketSummaryResponse>>> findAllByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("Tickets fetched", ticketService.findByCreatedByUserId(userId)));
    }

	@GetMapping("/{ticketId}")
	public ResponseEntity<ApiResponse<TicketResponse>> findByTicketId(@PathVariable String ticketId) {
		return ResponseEntity.ok(ApiResponse.success("Ticket fetched", ticketService.findByTicketId(ticketId)));
	}

	@GetMapping("/{ticketId}/comments")
	public ResponseEntity<ApiResponse<List<TicketCommentResponse>>> findComments(@PathVariable String ticketId) {
		return ResponseEntity.ok(ApiResponse.success("Ticket comments fetched", ticketService.findComments(ticketId)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<TicketResponse>> create(@Valid @RequestBody TicketCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Ticket created", ticketService.create(request)));
	}

	@PostMapping("/{ticketId}/comments")
	public ResponseEntity<ApiResponse<TicketCommentResponse>> addComment(@PathVariable String ticketId,
			@Valid @RequestBody TicketCommentCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Ticket comment created", ticketService.addComment(ticketId, request)));
	}

	@PostMapping("/{ticketId}/assign")
	public ResponseEntity<ApiResponse<TicketResponse>> assign(@PathVariable String ticketId,
			@Valid @RequestBody TicketAssignRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Ticket assigned", ticketService.assign(ticketId, request)));
	}

	@PutMapping("/{ticketId}")
	public ResponseEntity<ApiResponse<TicketResponse>> update(@PathVariable String ticketId,
			@Valid @RequestBody TicketUpdateRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Ticket updated", ticketService.update(ticketId, request)));
	}

	@PutMapping("/{ticketId}/comments/{commentId}")
	public ResponseEntity<ApiResponse<TicketCommentResponse>> updateComment(@PathVariable String ticketId,
			@PathVariable String commentId, @Valid @RequestBody TicketCommentUpdateRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Ticket comment updated", ticketService.updateComment(ticketId, commentId, request)));
	}

	@DeleteMapping("/{ticketId}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String ticketId) {
		ticketService.delete(ticketId);
		return ResponseEntity.ok(ApiResponse.success("Ticket deleted", null));
	}

	@DeleteMapping("/{ticketId}/comments/{commentId}")
	public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable String ticketId, @PathVariable String commentId,
			@Valid @RequestBody TicketCommentDeleteRequest request) {
		ticketService.deleteComment(ticketId, commentId, request);
		return ResponseEntity.ok(ApiResponse.success("Ticket comment deleted", null));
	}
}
