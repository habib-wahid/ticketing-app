package com.example.ticketing_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.ticketing_app.dto.ComplaintCategoryCreateRequest;
import com.example.ticketing_app.dto.ComplaintCategoryResponse;
import com.example.ticketing_app.dto.ComplaintCategoryUpdateRequest;
import com.example.ticketing_app.entity.ComplaintCategory;
import com.example.ticketing_app.exception.ConflictException;
import com.example.ticketing_app.exception.ForbiddenException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.repository.ComplaintCategoryRepository;

@Service
public class ComplaintCategoryService {

	private final ComplaintCategoryRepository complaintCategoryRepository;

	public ComplaintCategoryService(ComplaintCategoryRepository complaintCategoryRepository) {
		this.complaintCategoryRepository = complaintCategoryRepository;
	}

	public List<ComplaintCategoryResponse> findAll() {
		return complaintCategoryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	public ComplaintCategoryResponse findById(String id) {
		return toResponse(getCategoryEntity(id));
	}

	public ComplaintCategoryResponse create(ComplaintCategoryCreateRequest request, ActorContext actor) {
		if (!actor.isAdmin()) {
			throw new ForbiddenException("Only admins can create complaint categories");
		}

		String normalizedName = normalizeName(request.name());
		if (complaintCategoryRepository.existsByNameIgnoreCase(normalizedName)) {
			throw new ConflictException("Complaint category already exists: " + normalizedName);
		}

		LocalDateTime now = LocalDateTime.now();
		ComplaintCategory category = new ComplaintCategory();
		category.setName(normalizedName);
		category.setCreatedBy(actor.userId());
		category.setCreatedAt(now);
		category.setUpdatedBy(actor.userId());
		category.setUpdatedAt(now);

		return toResponse(complaintCategoryRepository.save(category));
	}

	public ComplaintCategoryResponse update(String id, ComplaintCategoryUpdateRequest request, ActorContext actor) {
		ComplaintCategory category = getCategoryEntity(id);

		if (StringUtils.hasText(request.name())) {
			String normalizedName = normalizeName(request.name());
			if (!normalizedName.equalsIgnoreCase(category.getName())
					&& complaintCategoryRepository.existsByNameIgnoreCase(normalizedName)) {
				throw new ConflictException("Complaint category already exists: " + normalizedName);
			}
			category.setName(normalizedName);
		}

		category.setUpdatedBy(actor.userId());
		category.setUpdatedAt(LocalDateTime.now());
		return toResponse(complaintCategoryRepository.save(category));
	}

	public void delete(String id) {
		ComplaintCategory category = getCategoryEntity(id);
		complaintCategoryRepository.delete(category);
	}

	private ComplaintCategory getCategoryEntity(String id) {
		return complaintCategoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Complaint category not found: " + id));
	}

	private ComplaintCategoryResponse toResponse(ComplaintCategory category) {
		return new ComplaintCategoryResponse(
				category.getId(),
				category.getName(),
				category.getCreatedBy(),
				category.getCreatedAt(),
				category.getUpdatedBy(),
				category.getUpdatedAt());
	}

	private String normalizeName(String name) {
		return StringUtils.hasText(name) ? name.trim() : null;
	}
}

