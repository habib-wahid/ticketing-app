package com.example.ticketing_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.ticketing_app.dto.CategoryDistributorMappingCreateRequest;
import com.example.ticketing_app.dto.CategoryDistributorMappingResponse;
import com.example.ticketing_app.dto.CategoryDistributorMappingUpdateRequest;
import com.example.ticketing_app.entity.CategoryDistributorMapping;
import com.example.ticketing_app.entity.ComplaintCategory;
import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.entity.UserRole;
import com.example.ticketing_app.exception.BadRequestException;
import com.example.ticketing_app.exception.ConflictException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.repository.CategoryDistributorMappingRepository;
import com.example.ticketing_app.repository.ComplaintCategoryRepository;
import com.example.ticketing_app.repository.UserRepository;

@Service
public class CategoryDistributorMappingService {

	private final CategoryDistributorMappingRepository mappingRepository;
	private final ComplaintCategoryRepository complaintCategoryRepository;
	private final UserRepository userRepository;

	public CategoryDistributorMappingService(CategoryDistributorMappingRepository mappingRepository,
			ComplaintCategoryRepository complaintCategoryRepository, UserRepository userRepository) {
		this.mappingRepository = mappingRepository;
		this.complaintCategoryRepository = complaintCategoryRepository;
		this.userRepository = userRepository;
	}

	public List<CategoryDistributorMappingResponse> findAll() {
		return mappingRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	public CategoryDistributorMappingResponse findById(String id) {
		return toResponse(getMappingEntity(id));
	}

	public CategoryDistributorMappingResponse findByCategoryId(String categoryId) {
		return mappingRepository.findByCategoryId(categoryId)
				.map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException(
						"No distributor mapping found for category: " + categoryId));
	}

	public CategoryDistributorMappingResponse create(CategoryDistributorMappingCreateRequest request,
			ActorContext actor) {
		String categoryId = request.categoryId().trim();
		if (mappingRepository.existsByCategoryId(categoryId)) {
			throw new ConflictException("Distributor already mapped for category: " + categoryId);
		}

		ComplaintCategory category = complaintCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Complaint category not found: " + categoryId));
		User distributor = resolveActiveDistributor(request.distributorUserId());

		LocalDateTime now = LocalDateTime.now();
		CategoryDistributorMapping mapping = new CategoryDistributorMapping();
		mapping.setCategoryId(category.getId());
		mapping.setCategoryName(category.getName());
		mapping.setDistributorUserId(distributor.getUserId());
		mapping.setDistributorName(buildFullName(distributor));
		mapping.setActive(request.active() == null || request.active());
		mapping.setCreatedBy(actor.userId());
		mapping.setCreatedAt(now);
		mapping.setUpdatedBy(actor.userId());
		mapping.setUpdatedAt(now);

		return toResponse(mappingRepository.save(mapping));
	}

	public CategoryDistributorMappingResponse update(String id, CategoryDistributorMappingUpdateRequest request,
			ActorContext actor) {
		CategoryDistributorMapping mapping = getMappingEntity(id);

		if (StringUtils.hasText(request.distributorUserId())) {
			User distributor = resolveActiveDistributor(request.distributorUserId());
			mapping.setDistributorUserId(distributor.getUserId());
			mapping.setDistributorName(buildFullName(distributor));
		}
		if (request.active() != null) {
			mapping.setActive(request.active());
		}

		mapping.setUpdatedBy(actor.userId());
		mapping.setUpdatedAt(LocalDateTime.now());
		return toResponse(mappingRepository.save(mapping));
	}

	public void delete(String id) {
		CategoryDistributorMapping mapping = getMappingEntity(id);
		mappingRepository.delete(mapping);
	}

	private User resolveActiveDistributor(String distributorUserId) {
		String normalizedId = distributorUserId.trim();
		User user = userRepository.findByUserId(normalizedId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + normalizedId));
		if (user.getRole() != UserRole.DISTRIBUTOR) {
			throw new BadRequestException("Mapped user must have role DISTRIBUTOR");
		}
		if (!user.isActive()) {
			throw new BadRequestException("Distributor must be active");
		}
		return user;
	}

	private CategoryDistributorMapping getMappingEntity(String id) {
		return mappingRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category distributor mapping not found: " + id));
	}

	private CategoryDistributorMappingResponse toResponse(CategoryDistributorMapping mapping) {
		return new CategoryDistributorMappingResponse(
				mapping.getId(),
				mapping.getCategoryId(),
				mapping.getCategoryName(),
				mapping.getDistributorUserId(),
				mapping.getDistributorName(),
				mapping.isActive(),
				mapping.getCreatedBy(),
				mapping.getCreatedAt(),
				mapping.getUpdatedBy(),
				mapping.getUpdatedAt());
	}

	private String buildFullName(User user) {
		String first = StringUtils.hasText(user.getFirstName()) ? user.getFirstName().trim() : null;
		String last = StringUtils.hasText(user.getLastName()) ? user.getLastName().trim() : null;
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
}
