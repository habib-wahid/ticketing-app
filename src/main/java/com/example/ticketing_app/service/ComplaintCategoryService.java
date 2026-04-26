package com.example.ticketing_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.ticketing_app.dto.ComplaintCategoryCreateRequest;
import com.example.ticketing_app.dto.ComplaintCategoryResponse;
import com.example.ticketing_app.dto.ComplaintCategoryUpdateRequest;
import com.example.ticketing_app.entity.ComplaintCategory;
import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.exception.ConflictException;
import com.example.ticketing_app.exception.ForbiddenException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.repository.ComplaintCategoryRepository;
import com.example.ticketing_app.repository.UserRepository;

@Service
public class ComplaintCategoryService {

	private final ComplaintCategoryRepository complaintCategoryRepository;
	private final UserRepository userRepository;

	public ComplaintCategoryService(ComplaintCategoryRepository complaintCategoryRepository,
			UserRepository userRepository) {
		this.complaintCategoryRepository = complaintCategoryRepository;
		this.userRepository = userRepository;
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

	public Page<ComplaintCategoryResponse> findAll(String name, Pageable pageable) {
		if (StringUtils.hasText(name)) {
			return complaintCategoryRepository.findByNameContainingIgnoreCase(name.trim(), pageable)
					.map(this::toResponse);
		}
		return complaintCategoryRepository.findAll(pageable).map(this::toResponse);
	}

	private ComplaintCategory getCategoryEntity(String id) {
		return complaintCategoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Complaint category not found: " + id));
	}

	private ComplaintCategoryResponse toResponse(ComplaintCategory category) {
		return new ComplaintCategoryResponse(
				category.getId(),
				category.getName(),
				resolveUserName(category.getCreatedBy()),
				category.getCreatedAt(),
				resolveUserName(category.getUpdatedBy()),
				category.getUpdatedAt());
	}

	private String resolveUserName(String userId) {
		if (!StringUtils.hasText(userId)) {
			return null;
		}
		return userRepository.findByUserId(userId)
				.map(this::buildFullName)
				.orElse(userId);
	}

	private String buildFullName(User user) {
		String first = normalizeName(user.getFirstName());
		String last = normalizeName(user.getLastName());
		if (first == null && last == null) {
			return user.getUserId();
		}
		if (first == null) {
			return last;
		}
		if (last == null) {
			return first;
		}
		return first + " " + last;
	}

	private String normalizeName(String name) {
		return StringUtils.hasText(name) ? name.trim() : null;
	}
}
