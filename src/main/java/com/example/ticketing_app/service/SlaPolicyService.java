package com.example.ticketing_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.ticketing_app.dto.SlaPolicyCreateRequest;
import com.example.ticketing_app.dto.SlaPolicyResponse;
import com.example.ticketing_app.dto.SlaPolicyUpdateRequest;
import com.example.ticketing_app.entity.SlaPolicy;
import com.example.ticketing_app.exception.ConflictException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.repository.SlaPolicyRepository;

@Service
public class SlaPolicyService {

	private final SlaPolicyRepository slaPolicyRepository;

	public SlaPolicyService(SlaPolicyRepository slaPolicyRepository) {
		this.slaPolicyRepository = slaPolicyRepository;
	}

	public List<SlaPolicyResponse> findAll() {
		return slaPolicyRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	public SlaPolicyResponse findById(String id) {
		return toResponse(getPolicyEntity(id));
	}

	public SlaPolicyResponse create(SlaPolicyCreateRequest request) {
		if (slaPolicyRepository.existsByPriority(request.priority())) {
			throw new ConflictException("SLA policy already exists for priority: " + request.priority());
		}

		SlaPolicy policy = new SlaPolicy();
		policy.setPriority(request.priority());
		policy.setResponseTimeHours(request.responseTimeHours());
		policy.setResolutionTimeHours(request.resolutionTimeHours());
		policy.setEscalationAfterHours(request.escalationAfterHours());
		policy.setReminderIntervalMinutes(request.reminderIntervalMinutes());
		policy.setActive(request.active() == null || request.active());
		policy.setUpdatedBy(normalizeUpdatedBy(request.updatedBy()));
		policy.setCreatedAt(LocalDateTime.now());
		policy.setUpdatedAt(LocalDateTime.now());

		return toResponse(slaPolicyRepository.save(policy));
	}

	public SlaPolicyResponse update(String id, SlaPolicyUpdateRequest request) {
		SlaPolicy policy = getPolicyEntity(id);

		if (request.responseTimeHours() != null) {
			policy.setResponseTimeHours(request.responseTimeHours());
		}
		if (request.resolutionTimeHours() != null) {
			policy.setResolutionTimeHours(request.resolutionTimeHours());
		}
		if (request.escalationAfterHours() != null) {
			policy.setEscalationAfterHours(request.escalationAfterHours());
		}
		if (request.reminderIntervalMinutes() != null) {
			policy.setReminderIntervalMinutes(request.reminderIntervalMinutes());
		}
		if (request.active() != null) {
			policy.setActive(request.active());
		}
		if (request.updatedBy() != null) {
			policy.setUpdatedBy(normalizeUpdatedBy(request.updatedBy()));
		}

		policy.setUpdatedAt(LocalDateTime.now());
		return toResponse(slaPolicyRepository.save(policy));
	}

	public void delete(String id) {
		SlaPolicy policy = getPolicyEntity(id);
		slaPolicyRepository.delete(policy);
	}

	public SlaPolicy findPolicyByPriority(com.example.ticketing_app.entity.TicketPriority priority) {
		return slaPolicyRepository.findByPriority(priority).orElse(null);
	}

	private SlaPolicy getPolicyEntity(String id) {
		return slaPolicyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("SLA policy not found: " + id));
	}

	private SlaPolicyResponse toResponse(SlaPolicy policy) {
		return new SlaPolicyResponse(
				policy.getId(),
				policy.getPriority(),
				policy.getResponseTimeHours(),
				policy.getResolutionTimeHours(),
				policy.getEscalationAfterHours(),
				policy.getReminderIntervalMinutes(),
				policy.isActive(),
				policy.getUpdatedBy(),
				policy.getUpdatedAt(),
				policy.getCreatedAt());
	}

	private String normalizeUpdatedBy(String updatedBy) {
		return StringUtils.hasText(updatedBy) ? updatedBy.trim() : "SYSTEM";
	}
}

