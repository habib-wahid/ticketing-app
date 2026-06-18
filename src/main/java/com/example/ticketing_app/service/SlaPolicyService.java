package com.example.ticketing_app.service;

import com.example.ticketing_app.dto.SlaPolicyCreateRequest;
import com.example.ticketing_app.dto.SlaPolicyResponse;
import com.example.ticketing_app.dto.SlaPolicyUpdateRequest;
import com.example.ticketing_app.entity.ComplaintCategory;
import com.example.ticketing_app.entity.SlaPolicy;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.exception.ConflictException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.mapper.SlaPolicyMapper;
import com.example.ticketing_app.repository.ComplaintCategoryRepository;
import com.example.ticketing_app.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaPolicyService {

	private final SlaPolicyRepository slaPolicyRepository;
	private final ComplaintCategoryRepository complaintCategoryRepository;
	private final SlaPolicyMapper slaPolicyMapper;

	public SlaPolicyService(SlaPolicyRepository slaPolicyRepository,
			ComplaintCategoryRepository complaintCategoryRepository, SlaPolicyMapper slaPolicyMapper) {
		this.slaPolicyRepository = slaPolicyRepository;
		this.complaintCategoryRepository = complaintCategoryRepository;
		this.slaPolicyMapper = slaPolicyMapper;
	}

	public List<SlaPolicyResponse> findAll() {
		return slaPolicyMapper.toResponseList(slaPolicyRepository.findAll());
	}

	public SlaPolicyResponse findById(String id) {
		return slaPolicyMapper.toResponse(getPolicyEntity(id));
	}

	public SlaPolicyResponse create(SlaPolicyCreateRequest request) {
		ComplaintCategory category = resolveCategory(request.complaintCategoryId());
		String name = normalizeName(request.name());
		if (slaPolicyRepository.existsByNameIgnoreCase(name)) {
			throw new ConflictException("SLA policy already exists with name: " + name);
		}

		SlaPolicy policy = slaPolicyMapper.toEntity(request);
		policy.setName(name);
		policy.setCategoryId(category.getId());
		policy.setCategoryName(category.getName());
		policy.setActive(request.active() == null || request.active());
		policy.setUpdatedBy(normalizeUpdatedBy(request.updatedBy()));
		LocalDateTime now = LocalDateTime.now();
		policy.setCreatedAt(now);
		policy.setUpdatedAt(now);

		return slaPolicyMapper.toResponse(slaPolicyRepository.save(policy));
	}

	public SlaPolicyResponse update(String id, SlaPolicyUpdateRequest request) {
		SlaPolicy policy = getPolicyEntity(id);

		slaPolicyMapper.updateEntity(request, policy);
		if (request.name() != null) {
			policy.setName(normalizeName(request.name()));
		}
		if (request.updatedBy() != null) {
			policy.setUpdatedBy(normalizeUpdatedBy(request.updatedBy()));
		}
		policy.setUpdatedAt(LocalDateTime.now());

		return slaPolicyMapper.toResponse(slaPolicyRepository.save(policy));
	}

	public void delete(String id) {
		SlaPolicy policy = getPolicyEntity(id);
		slaPolicyRepository.delete(policy);
	}

	public SlaPolicy findPolicy(String categoryId, TicketPriority priority) {
		if (!StringUtils.hasText(categoryId) || priority == null) {
			return null;
		}
		return slaPolicyRepository.findByCategoryIdAndPriority(categoryId, priority).orElse(null);
	}

	private ComplaintCategory resolveCategory(String complaintCategoryId) {
		String normalizedId = StringUtils.hasText(complaintCategoryId) ? complaintCategoryId.trim() : null;
		if (normalizedId == null) {
			throw new ResourceNotFoundException("Complaint category id is required");
		}
		return complaintCategoryRepository.findById(normalizedId)
				.orElseThrow(() -> new ResourceNotFoundException("Complaint category not found: " + normalizedId));
	}

	private SlaPolicy getPolicyEntity(String id) {
		return slaPolicyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("SLA policy not found: " + id));
	}

	private String normalizeUpdatedBy(String updatedBy) {
		return StringUtils.hasText(updatedBy) ? updatedBy.trim() : "SYSTEM";
	}

	private String normalizeName(String name) {
		return StringUtils.hasText(name) ? name.trim() : name;
	}
}

