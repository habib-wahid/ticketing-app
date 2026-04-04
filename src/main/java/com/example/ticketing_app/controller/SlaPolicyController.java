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

import com.example.ticketing_app.dto.SlaPolicyCreateRequest;
import com.example.ticketing_app.dto.SlaPolicyResponse;
import com.example.ticketing_app.dto.SlaPolicyUpdateRequest;
import com.example.ticketing_app.service.SlaPolicyService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sla-policies")
@Tag(name = "SLA Policies")
public class SlaPolicyController {

	private final SlaPolicyService slaPolicyService;

	public SlaPolicyController(SlaPolicyService slaPolicyService) {
		this.slaPolicyService = slaPolicyService;
	}

	@GetMapping
	public List<SlaPolicyResponse> findAll() {
		return slaPolicyService.findAll();
	}

	@GetMapping("/{id}")
	public SlaPolicyResponse findById(@PathVariable String id) {
		return slaPolicyService.findById(id);
	}

	@PostMapping
	public ResponseEntity<SlaPolicyResponse> create(@Valid @RequestBody SlaPolicyCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(slaPolicyService.create(request));
	}

	@PutMapping("/{id}")
	public SlaPolicyResponse update(@PathVariable String id, @Valid @RequestBody SlaPolicyUpdateRequest request) {
		return slaPolicyService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		slaPolicyService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
