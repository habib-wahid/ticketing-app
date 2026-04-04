package com.example.ticketing_app.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.ticketing_app.dto.TicketCreateRequest;
import com.example.ticketing_app.dto.TicketResponse;
import com.example.ticketing_app.dto.TicketUpdateRequest;
import com.example.ticketing_app.entity.SlaPolicy;
import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;
import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.entity.UserRole;
import com.example.ticketing_app.exception.BadRequestException;
import com.example.ticketing_app.exception.ConflictException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.repository.TicketRepository;
import com.example.ticketing_app.repository.UserRepository;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;
	private final SlaPolicyService slaPolicyService;

	public TicketService(TicketRepository ticketRepository, UserRepository userRepository, SlaPolicyService slaPolicyService) {
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
		this.slaPolicyService = slaPolicyService;
	}

	public List<TicketResponse> findAll() {
		return ticketRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	public TicketResponse findByTicketId(String ticketId) {
		return toResponse(getTicketEntity(ticketId));
	}

	public TicketResponse create(TicketCreateRequest request) {
		if (!StringUtils.hasText(request.createdByUserId())) {
			throw new BadRequestException("createdByUserId is required");
		}

		User createdBy = userRepository.findByUserId(request.createdByUserId())
				.orElseThrow(() -> new ResourceNotFoundException("Created-by user not found: " + request.createdByUserId()));

		String assignedToUserId = normalize(request.assignedToUserId());
		if (assignedToUserId != null) {
			User assignee = userRepository.findByUserId(assignedToUserId)
					.orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + assignedToUserId));
			if (assignee.getRole() == UserRole.CUSTOMER) {
				throw new BadRequestException("Tickets can only be assigned to agents or admins");
			}
		}

		LocalDateTime now = LocalDateTime.now();
		Ticket ticket = new Ticket();
		ticket.setTicketId(generateTicketId());
		ticket.setTitle(request.title().trim());
		ticket.setDescription(request.description().trim());
		ticket.setCategory(request.category());
		ticket.setPriority(request.priority());
		ticket.setStatus(StringUtils.hasText(assignedToUserId) ? TicketStatus.ASSIGNED : TicketStatus.NEW);
		ticket.setCreatedByUserId(createdBy.getUserId());
		ticket.setAssignedToUserId(assignedToUserId);
		if (assignedToUserId != null) {
			ticket.setAssignedAt(now);
		}
		applySlaPolicy(ticket, ticket.getPriority(), now);
		ticket.setTags(normalizeTags(request.tags()));
		ticket.setCustomFields(request.customFields() == null ? new HashMap<>() : new HashMap<>(request.customFields()));
		ticket.setCreatedAt(now);
		ticket.setUpdatedAt(now);

		return toResponse(ticketRepository.save(ticket));
	}

	public TicketResponse update(String ticketId, TicketUpdateRequest request) {
		Ticket ticket = getTicketEntity(ticketId);
		LocalDateTime now = LocalDateTime.now();

		if (StringUtils.hasText(request.title())) {
			ticket.setTitle(request.title().trim());
		}
		if (StringUtils.hasText(request.description())) {
			ticket.setDescription(request.description().trim());
		}
		if (request.category() != null) {
			ticket.setCategory(request.category());
		}
		if (request.priority() != null && request.priority() != ticket.getPriority()) {
			ticket.setPriority(request.priority());
			applySlaPolicy(ticket, request.priority(), ticket.getCreatedAt() == null ? now : ticket.getCreatedAt());
		}

		String assignedToUserId = request.assignedToUserId() == null ? null : normalize(request.assignedToUserId());
		if (assignedToUserId != null) {
			User assignee = userRepository.findByUserId(assignedToUserId)
					.orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + assignedToUserId));
			if (assignee.getRole() == UserRole.CUSTOMER) {
				throw new BadRequestException("Tickets can only be assigned to agents or admins");
			}
			ticket.setAssignedToUserId(assignedToUserId);
			ticket.setAssignedAt(now);
		}

		if (request.status() != null) {
			ticket.setStatus(request.status());
			applyStatusTimestamps(ticket, request.status(), now);
		}
		applySlaState(ticket, now);
		if (request.tags() != null) {
			ticket.setTags(normalizeTags(request.tags()));
		}
		if (request.customFields() != null) {
			ticket.setCustomFields(new HashMap<>(request.customFields()));
		}

		ticket.setUpdatedAt(now);
		return toResponse(ticketRepository.save(ticket));
	}

	public void delete(String ticketId) {
		Ticket ticket = getTicketEntity(ticketId);
		ticketRepository.delete(ticket);
	}

	private Ticket getTicketEntity(String ticketId) {
		return ticketRepository.findByTicketId(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
	}

	private TicketResponse toResponse(Ticket ticket) {
		return new TicketResponse(
				ticket.getTicketId(),
				ticket.getTitle(),
				ticket.getDescription(),
				ticket.getCategory(),
				ticket.getPriority(),
				ticket.getStatus(),
				ticket.getCreatedByUserId(),
				ticket.getAssignedToUserId(),
				ticket.getAssignedAt(),
				ticket.getResolvedAt(),
				ticket.getClosedAt(),
				ticket.getSlaDeadline(),
				ticket.getResponseDeadline(),
				ticket.getEscalationDueAt(),
				ticket.getNextReminderAt(),
				ticket.getSlaBreachedAt(),
				ticket.getEscalationLevel(),
				ticket.getTags(),
				ticket.getCustomFields(),
				ticket.getCreatedAt(),
				ticket.getUpdatedAt());
	}

	private String generateTicketId() {
		String ticketId;
		do {
			ticketId = "TKT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		} while (ticketRepository.existsByTicketId(ticketId));
		return ticketId;
	}

	private void applySlaPolicy(Ticket ticket, TicketPriority priority, LocalDateTime createdAt) {
		SlaPolicy policy = slaPolicyService.findPolicyByPriority(priority);
		int responseHours = policy != null ? policy.getResponseTimeHours() : defaultResponseHours(priority);
		int resolutionHours = policy != null ? policy.getResolutionTimeHours() : defaultResolutionHours(priority);
		int escalationHours = policy != null ? policy.getEscalationAfterHours() : defaultEscalationHours(priority);
		int reminderMinutes = policy != null ? policy.getReminderIntervalMinutes() : defaultReminderMinutes(priority);

		LocalDateTime responseDeadline = createdAt.plusHours(responseHours);
		LocalDateTime slaDeadline = createdAt.plusHours(resolutionHours);
		LocalDateTime escalationDueAt = createdAt.plusHours(escalationHours);
		LocalDateTime reminderAt = slaDeadline.minusMinutes(reminderMinutes);
		if (reminderAt.isBefore(createdAt)) {
			reminderAt = createdAt;
		}

		ticket.setResponseDeadline(responseDeadline);
		ticket.setSlaDeadline(slaDeadline);
		ticket.setEscalationDueAt(escalationDueAt);
		ticket.setNextReminderAt(reminderAt);
	}

	private void applySlaState(Ticket ticket, LocalDateTime now) {
		if (ticket.getSlaDeadline() != null && ticket.getSlaBreachedAt() == null
				&& now.isAfter(ticket.getSlaDeadline())) {
			ticket.setSlaBreachedAt(now);
		}
		if (ticket.getEscalationDueAt() != null && ticket.getEscalationLevel() != null
				&& ticket.getEscalationLevel() == 0 && now.isAfter(ticket.getEscalationDueAt())) {
			ticket.setEscalationLevel(1);
		}

		SlaPolicy policy = slaPolicyService.findPolicyByPriority(ticket.getPriority());
		int reminderMinutes = policy != null ? policy.getReminderIntervalMinutes() : defaultReminderMinutes(ticket.getPriority());
		if (ticket.getNextReminderAt() != null && now.isAfter(ticket.getNextReminderAt())) {
			ticket.setNextReminderAt(now.plusMinutes(reminderMinutes));
		}
	}

	private int defaultResponseHours(TicketPriority priority) {
		return switch (priority) {
			case LOW -> 24;
			case MEDIUM -> 8;
			case HIGH -> 2;
			case CRITICAL -> 1;
		};
	}

	private int defaultResolutionHours(TicketPriority priority) {
		return switch (priority) {
			case LOW -> 72;
			case MEDIUM -> 24;
			case HIGH -> 8;
			case CRITICAL -> 4;
		};
	}

	private int defaultEscalationHours(TicketPriority priority) {
		return switch (priority) {
			case LOW -> 48;
			case MEDIUM -> 12;
			case HIGH -> 4;
			case CRITICAL -> 2;
		};
	}

	private int defaultReminderMinutes(TicketPriority priority) {
		return switch (priority) {
			case LOW -> 60;
			case MEDIUM -> 30;
			case HIGH -> 15;
			case CRITICAL -> 5;
		};
	}

	private void applyStatusTimestamps(Ticket ticket, TicketStatus status, LocalDateTime now) {
		switch (status) {
			case RESOLVED -> ticket.setResolvedAt(now);
			case CLOSED -> ticket.setClosedAt(now);
			case REOPENED -> {
				ticket.setResolvedAt(null);
				ticket.setClosedAt(null);
			}
			default -> {
				// no-op for basic CRUD
			}
		}
	}

	private List<String> normalizeTags(List<String> tags) {
		if (tags == null) {
			return new ArrayList<>();
		}
		return tags.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.toList());
	}

	private String normalize(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}

