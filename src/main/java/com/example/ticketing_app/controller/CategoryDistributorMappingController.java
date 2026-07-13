package com.example.ticketing_app.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing_app.dto.ApiResponse;
import com.example.ticketing_app.dto.CategoryDistributorMappingCreateRequest;
import com.example.ticketing_app.dto.CategoryDistributorMappingResponse;
import com.example.ticketing_app.dto.CategoryDistributorMappingUpdateRequest;
import com.example.ticketing_app.security.UserPrincipal;
import com.example.ticketing_app.service.ActorContext;
import com.example.ticketing_app.service.CategoryDistributorMappingService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/category-distributor-mappings")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Category Distributor Mappings")
public class CategoryDistributorMappingController {

	private final CategoryDistributorMappingService mappingService;

	public CategoryDistributorMappingController(CategoryDistributorMappingService mappingService) {
		this.mappingService = mappingService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<CategoryDistributorMappingResponse>>> findAll() {
		return ResponseEntity.ok(ApiResponse.success("Category distributor mappings fetched", mappingService.findAll()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<CategoryDistributorMappingResponse>> findById(@PathVariable String id) {
		return ResponseEntity.ok(ApiResponse.success("Category distributor mapping fetched", mappingService.findById(id)));
	}

	@GetMapping("/by-category/{categoryId}")
	public ResponseEntity<ApiResponse<CategoryDistributorMappingResponse>> findByCategoryId(
			@PathVariable String categoryId) {
		return ResponseEntity.ok(ApiResponse.success("Category distributor mapping fetched",
				mappingService.findByCategoryId(categoryId)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<CategoryDistributorMappingResponse>> create(
			@Valid @RequestBody CategoryDistributorMappingCreateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Category distributor mapping created",
						mappingService.create(request, actor(principal))));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<CategoryDistributorMappingResponse>> update(
			@PathVariable String id,
			@Valid @RequestBody CategoryDistributorMappingUpdateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Category distributor mapping updated",
				mappingService.update(id, request, actor(principal))));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
		mappingService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Category distributor mapping deleted", null));
	}

	private ActorContext actor(UserPrincipal principal) {
		return new ActorContext(principal.getUserId(), principal.getRole());
	}
}
