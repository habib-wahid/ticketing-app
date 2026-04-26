package com.example.ticketing_app.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing_app.dto.ApiResponse;
import com.example.ticketing_app.dto.ComplaintCategoryCreateRequest;
import com.example.ticketing_app.dto.ComplaintCategoryResponse;
import com.example.ticketing_app.dto.ComplaintCategoryUpdateRequest;
import com.example.ticketing_app.security.UserPrincipal;
import com.example.ticketing_app.service.ActorContext;
import com.example.ticketing_app.service.ComplaintCategoryService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/complaint-categories")
@Tag(name = "Complaint Categories")
public class ComplaintCategoryController {

	private final ComplaintCategoryService complaintCategoryService;

	public ComplaintCategoryController(ComplaintCategoryService complaintCategoryService) {
		this.complaintCategoryService = complaintCategoryService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<ComplaintCategoryResponse>>> findAll(
			@RequestParam(required = false) String name,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
		return ResponseEntity.ok(ApiResponse.success("Complaint categories fetched",
				complaintCategoryService.findAll(name, pageRequest)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ComplaintCategoryResponse>> findById(@PathVariable String id) {
		return ResponseEntity.ok(ApiResponse.success("Complaint category fetched", complaintCategoryService.findById(id)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ComplaintCategoryResponse>> create(
			@Valid @RequestBody ComplaintCategoryCreateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Complaint category created", complaintCategoryService.create(request, actor(principal))));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ComplaintCategoryResponse>> update(
			@PathVariable String id,
			@Valid @RequestBody ComplaintCategoryUpdateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Complaint category updated",
				complaintCategoryService.update(id, request, actor(principal))));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
		complaintCategoryService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Complaint category deleted", null));
	}

	private ActorContext actor(UserPrincipal principal) {
		return new ActorContext(principal.getUserId(), principal.getRole());
	}
}
