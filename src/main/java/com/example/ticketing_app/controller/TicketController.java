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

import com.example.ticketing_app.dto.TicketCreateRequest;
import com.example.ticketing_app.dto.TicketResponse;
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
	public List<TicketResponse> findAll() {
		return ticketService.findAll();
	}

	@GetMapping("/{ticketId}")
	public TicketResponse findByTicketId(@PathVariable String ticketId) {
		return ticketService.findByTicketId(ticketId);
	}

	@PostMapping
	public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
	}

	@PutMapping("/{ticketId}")
	public TicketResponse update(@PathVariable String ticketId, @Valid @RequestBody TicketUpdateRequest request) {
		return ticketService.update(ticketId, request);
	}

	@DeleteMapping("/{ticketId}")
	public ResponseEntity<Void> delete(@PathVariable String ticketId) {
		ticketService.delete(ticketId);
		return ResponseEntity.noContent().build();
	}
}
