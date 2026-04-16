package com.example.ticketing_app.service;

import com.example.ticketing_app.dto.TicketAssignRequest;
import com.example.ticketing_app.dto.TicketAssignedToResponse;
import com.example.ticketing_app.dto.TicketAttachmentResponse;
import com.example.ticketing_app.dto.CommentAuthorResponse;
import com.example.ticketing_app.dto.TicketCommentCreateRequest;
import com.example.ticketing_app.dto.TicketCommentDeleteRequest;
import com.example.ticketing_app.dto.TicketCommentResponse;
import com.example.ticketing_app.dto.TicketCommentUpdateRequest;
import com.example.ticketing_app.dto.TicketCreateRequest;
import com.example.ticketing_app.dto.TicketCreatedByResponse;
import com.example.ticketing_app.dto.TicketResponse;
import com.example.ticketing_app.dto.TicketSlaEventResponse;
import com.example.ticketing_app.dto.TicketSlaSummary;
import com.example.ticketing_app.dto.TicketStatusChangeRequest;
import com.example.ticketing_app.dto.TicketStatusHistoryResponse;
import com.example.ticketing_app.dto.TicketSummaryResponse;
import com.example.ticketing_app.dto.TicketUpdateRequest;
import com.example.ticketing_app.entity.SlaPolicy;
import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketAssignedTo;
import com.example.ticketing_app.entity.TicketAttachment;
import com.example.ticketing_app.entity.CommentAuthor;
import com.example.ticketing_app.entity.TicketComment;
import com.example.ticketing_app.entity.TicketCreatedBy;
import com.example.ticketing_app.entity.TicketFilterStatus;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketSlaEvent;
import com.example.ticketing_app.entity.TicketStatus;
import com.example.ticketing_app.entity.TicketStatusHistory;
import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.entity.UserRole;
import com.example.ticketing_app.exception.BadRequestException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.repository.TicketRepository;
import com.example.ticketing_app.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private static final int MAX_TAGS = 5;
    private static final String SYSTEM_ACTOR = "system";

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SlaPolicyService slaPolicyService;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, SlaPolicyService slaPolicyService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.slaPolicyService = slaPolicyService;
    }

    public List<TicketSummaryResponse> findAll() {
		List<Ticket> tickets = ticketRepository.findAllSummary();
		return tickets.stream().map(this::toSummaryResponse).collect(Collectors.toList());
	}

    public Page<TicketSummaryResponse> findByCreatedByUserId(String userId, TicketFilterStatus status, Pageable pageable) {
        List<TicketStatus> statuses = null;
        if (status == TicketFilterStatus.PENDING) {
            statuses = List.of(TicketStatus.NEW, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.REOPENED);
        } else if (status == TicketFilterStatus.RESOLVED) {
            statuses = List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED);
        }

        Page<Ticket> page;
        if (statuses == null) {
            page = ticketRepository.findByCreatedByUserId(userId, pageable);
        } else {
            page = ticketRepository.findByCreatedByUserIdAndStatusIn(userId, statuses, pageable);
        }

        List<TicketSummaryResponse> responses = page.getContent().stream().map(this::toSummaryResponse).collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

	private Map<String, String> loadAssignedUserNames(List<Ticket> tickets) {
		return Collections.emptyMap();
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

        if (createdBy.getRole() == UserRole.CUSTOMER && assignedToUserId != null) {
            throw new BadRequestException("Customers cannot assign tickets to agents");
        }

        User assignee = null;
        if (assignedToUserId != null) {
            assignee = userRepository.findByUserId(assignedToUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + assignedToUserId));
            if (assignee.getRole() == UserRole.CUSTOMER) {
                throw new BadRequestException("Tickets can only be assigned to agents or admins");
            }
            if (!assignee.isActive()) {
                throw new BadRequestException("Assigned user must be active");
            }
        }

        String normalizedTitle = request.title().trim();

        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = new Ticket();
        ticket.setTicketId(generateTicketId());
        ticket.setTitle(normalizedTitle);
        ticket.setDescription(request.description().trim());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());
        ticket.setStatus(StringUtils.hasText(assignedToUserId) ? TicketStatus.ASSIGNED : TicketStatus.NEW);
        ticket.setCreatedBy(new TicketCreatedBy(buildFullName(createdBy), createdBy.getUserId(), createdBy.getRole()));
        if (assignee != null) {
            ticket.setAssignedTo(new TicketAssignedTo(buildFullName(assignee), assignee.getUserId(), assignee.getRole()));
        }

        if (assignedToUserId != null) {
            ticket.setAssignedAt(now);
            addStatusHistory(ticket, TicketStatus.NEW, TicketStatus.ASSIGNED, createdBy.getUserId(),
                    "Assigned on create");
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
        if (request.assignedToUserId() != null) {
            if (!StringUtils.hasText(assignedToUserId)) {
                ticket.setAssignedTo(null);
                ticket.setAssignedAt(null);
            } else {
                User assignee = userRepository.findByUserId(assignedToUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + assignedToUserId));
                if (assignee.getRole() == UserRole.CUSTOMER) {
                    throw new BadRequestException("Tickets can only be assigned to agents or admins");
                }
                if (!assignee.isActive()) {
                    throw new BadRequestException("Assigned user must be active");
                }
                ticket.setAssignedTo(new TicketAssignedTo(buildFullName(assignee), assignee.getUserId(), assignee.getRole()));
                ticket.setAssignedAt(now);
                if (ticket.getStatus() == TicketStatus.NEW) {
                    addStatusHistory(ticket, TicketStatus.NEW, TicketStatus.ASSIGNED, SYSTEM_ACTOR, "Assigned");
                    ticket.setStatus(TicketStatus.ASSIGNED);
                }
            }
        }

        if (request.status() != null) {
            TicketStatus targetStatus = request.status();
            TicketStatus previousStatus = ticket.getStatus();
            ticket.setStatus(targetStatus);
            applyStatusTimestamps(ticket, targetStatus, now);
            addStatusHistory(ticket, previousStatus, targetStatus, SYSTEM_ACTOR, "Status update");
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

    public List<TicketCommentResponse> findComments(String ticketId) {
        Ticket ticket = getTicketEntity(ticketId);
        return toCommentResponses(ticket.getComments());
    }

    public TicketCommentResponse addComment(String ticketId, TicketCommentCreateRequest request) {
        Ticket ticket = getTicketEntity(ticketId);
        User author = userRepository.findByUserId(request.authorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.authorUserId()));
        validateCommentAccess(author, ticket);

        LocalDateTime now = LocalDateTime.now();
        TicketComment comment = new TicketComment();
        comment.setCommentId(generateCommentId());
        comment.setAuthor(new CommentAuthor(author.getUserId(), buildFullName(author), author.getRole()));
        comment.setText(request.text().trim());
        comment.setInternal(Boolean.TRUE.equals(request.internal()));
        comment.setAttachments(normalizeAttachmentIds(request.attachments()));
        comment.setCreatedAt(now);
        ticket.getComments().add(comment);
        ticket.setUpdatedAt(now);

        ticketRepository.save(ticket);
        return toCommentResponse(comment);
    }

    public TicketCommentResponse updateComment(String ticketId, String commentId, TicketCommentUpdateRequest request) {
        Ticket ticket = getTicketEntity(ticketId);
        User actor = userRepository.findByUserId(request.updatedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.updatedByUserId()));
        TicketComment comment = getComment(ticket, commentId);
        validateCommentAccess(actor, ticket);
        if (actor.getRole() == UserRole.CUSTOMER && comment.isInternal()) {
            throw new BadRequestException("Customers cannot update internal comments");
        }
        if (actor.getRole() == UserRole.CUSTOMER && Boolean.TRUE.equals(request.internal())) {
            throw new BadRequestException("Customers cannot set internal comments");
        }

        if (StringUtils.hasText(request.text())) {
            comment.setText(request.text().trim());
        }
        if (request.internal() != null) {
            comment.setInternal(request.internal());
        }
        if (request.attachments() != null) {
            comment.setAttachments(normalizeAttachmentIds(request.attachments()));
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return toCommentResponse(comment);
    }

    public void deleteComment(String ticketId, String commentId, TicketCommentDeleteRequest request) {
        Ticket ticket = getTicketEntity(ticketId);
        User actor = userRepository.findByUserId(request.deletedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.deletedByUserId()));
        TicketComment comment = getComment(ticket, commentId);
        validateCommentAccess(actor, ticket);
        if (actor.getRole() == UserRole.CUSTOMER && comment.isInternal()) {
            throw new BadRequestException("Customers cannot delete internal comments");
        }

        ticket.getComments().remove(comment);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    public void delete(String ticketId) {
        Ticket ticket = getTicketEntity(ticketId);
        ticketRepository.delete(ticket);
    }

    public TicketResponse assign(String ticketId, TicketAssignRequest request) {
        Ticket ticket = getTicketEntity(ticketId);
        User actor = userRepository.findByUserId(request.assignedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.assignedByUserId()));
        if (actor.getRole() == UserRole.CUSTOMER) {
            throw new BadRequestException("Customers cannot assign tickets");
        }

        String assignedToUserId = normalize(request.assignedToUserId());
        User assignee = userRepository.findByUserId(assignedToUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + assignedToUserId));
        if (assignee.getRole() == UserRole.CUSTOMER) {
            throw new BadRequestException("Tickets can only be assigned to agents or admins");
        }
        if (!assignee.isActive()) {
            throw new BadRequestException("Assigned user must be active");
        }

        TicketStatus requestedStatus = TicketStatus.ASSIGNED;
        TicketStatus previousStatus = ticket.getStatus();
        ticket.setStatus(requestedStatus);

        LocalDateTime now = LocalDateTime.now();
        ticket.setAssignedTo(new TicketAssignedTo(buildFullName(assignee), assignee.getUserId(), assignee.getRole()));
        ticket.setAssignedAt(now);
        ticket.setUpdatedAt(now);
        addStatusHistory(ticket, previousStatus, requestedStatus, actor.getUserId(),
                StringUtils.hasText(request.reason()) ? request.reason().trim() : "Assigned");
        return toResponse(ticketRepository.save(ticket));
    }

    public TicketResponse changeStatus(String ticketId, TicketStatusChangeRequest request) {
        Ticket ticket = getTicketEntity(ticketId);
        User actor = userRepository.findByUserId(request.changedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.changedByUserId()));
        if (actor.getRole() == UserRole.CUSTOMER) {
            throw new BadRequestException("Customers cannot change tickets status");
        }

        TicketStatus targetStatus = request.status();
        TicketStatus previousStatus = ticket.getStatus();

        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus(targetStatus);
        applyStatusTimestamps(ticket, targetStatus, now);
        addStatusHistory(ticket, previousStatus, targetStatus, actor.getUserId(),
                StringUtils.hasText(request.reason()) ? request.reason().trim() : "Status changed");
        ticket.setUpdatedAt(now);
        return toResponse(ticketRepository.save(ticket));
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
                toCreatedByResponse(ticket),
                toAssignedToResponse(ticket.getAssignedTo()),
                ticket.getAssignedAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                buildSlaSummary(ticket),
                ticket.getResponseDeadline(),
                ticket.getEscalationDueAt(),
                ticket.getNextReminderAt(),
                ticket.getSlaBreachedAt(),
                ticket.getEscalationLevel(),
                toCommentResponses(ticket.getComments()),
                toAttachmentResponses(ticket.getAttachments()),
                toStatusHistoryResponses(ticket.getStatusHistory()),
                toSlaEventResponses(ticket.getSlaEvents()),
                ticket.getTags(),
                ticket.getCustomFields(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }

    private TicketSummaryResponse toSummaryResponse(Ticket ticket, Map<String, String> userNamesMap) {
		return toSummaryResponse(ticket);
	}

	private TicketSummaryResponse toSummaryResponse(Ticket ticket) {
		return new TicketSummaryResponse(
				ticket.getTicketId(),
				ticket.getTitle(),
				ticket.getDescription(),
				ticket.getCategory(),
				ticket.getPriority(),
				ticket.getStatus(),
				toCreatedByResponse(ticket),
				toAssignedToResponse(ticket.getAssignedTo()),
				ticket.getAssignedAt(),
				ticket.getResolvedAt(),
				ticket.getClosedAt(),
				buildSlaSummary(ticket),
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

	private TicketAssignedToResponse toAssignedToResponse(TicketAssignedTo assignedTo) {
        if (assignedTo == null) {
            return null;
        }
        String name = StringUtils.hasText(assignedTo.getName())
                ? assignedTo.getName()
                : resolveAssignedUserName(assignedTo.getUserId());
        return new TicketAssignedToResponse(assignedTo.getUserId(), name, assignedTo.getRole());
    }

    private String resolveAssignedName(TicketAssignedTo assignedTo) {
        if (assignedTo == null) {
            return null;
        }
        if (StringUtils.hasText(assignedTo.getName())) {
            return assignedTo.getName();
        }
        return resolveAssignedUserName(assignedTo.getUserId());
    }

    private String resolveAssignedUserName(String assignedToUserId) {
        if (!StringUtils.hasText(assignedToUserId)) {
            return null;
        }
        return userRepository.findByUserId(assignedToUserId)
                .map(this::buildFullName)
                .orElse(assignedToUserId);
    }

    private TicketComment getComment(Ticket ticket, String commentId) {
        return ticket.getComments().stream()
                .filter(comment -> commentId.equals(comment.getCommentId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }

    private String generateTicketId() {
        String ticketId;
        do {
            ticketId = "TKT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        } while (ticketRepository.existsByTicketId(ticketId));
        return ticketId;
    }

    private String generateCommentId() {
        return "cmt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
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
        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            return;
        }
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

    private void addStatusHistory(Ticket ticket, TicketStatus from, TicketStatus to, String actor, String reason) {
        TicketStatusHistory history = new TicketStatusHistory();
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(actor);
        history.setChangedAt(LocalDateTime.now());
        history.setReason(reason);
        ticket.getStatusHistory().add(history);
    }

    private void validateCommentAccess(User actor, Ticket ticket) {
        TicketCreatedBy createdBy = ticket.getCreatedBy();
        if (actor.getRole() == UserRole.CUSTOMER && (createdBy == null
                || !actor.getUserId().equals(createdBy.getUserId()))) {
            throw new BadRequestException("Customers can only comment on their own tickets");
        }
    }

    private List<String> normalizeAttachmentIds(List<String> attachments) {
        if (attachments == null) {
            return new ArrayList<>();
        }
        return attachments.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.toList());
    }

    private String buildFullName(User user) {
        String first = normalize(user.getFirstName());
        String last = normalize(user.getLastName());
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

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        List<String> normalized = tags.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.toList());
        if (normalized.size() > MAX_TAGS) {
            throw new BadRequestException("A ticket can have at most " + MAX_TAGS + " tags");
        }
        return normalized;
    }

    private TicketCreatedByResponse toCreatedByResponse(Ticket ticket) {
        TicketCreatedBy createdBy = ticket.getCreatedBy();
        if (createdBy == null) {
            return null;
        }
        String createdByName = StringUtils.hasText(createdBy.getName())
                ? createdBy.getName()
                : resolveAssignedUserName(createdBy.getUserId());
        return new TicketCreatedByResponse(createdBy.getUserId(), createdByName, createdBy.getRole());
    }

    private List<TicketCommentResponse> toCommentResponses(List<TicketComment> comments) {
        if (comments == null) {
            return List.of();
        }
        return comments.stream().map(this::toCommentResponse).collect(Collectors.toList());
    }

    private TicketCommentResponse toCommentResponse(TicketComment comment) {
        if (comment == null) {
            return null;
        }
        return new TicketCommentResponse(
                comment.getCommentId(),
                toCommentAuthorResponse(comment.getAuthor()),
                comment.getText(),
                comment.isInternal(),
                comment.getAttachments(),
                comment.getCreatedAt());
    }

    private CommentAuthorResponse toCommentAuthorResponse(CommentAuthor author) {
        if (author == null) {
            return null;
        }
        return new CommentAuthorResponse(author.getUserId(), author.getFullName(), author.getRole());
    }

    private List<TicketAttachmentResponse> toAttachmentResponses(List<TicketAttachment> attachments) {
        if (attachments == null) {
            return List.of();
        }
        return attachments.stream().map(this::toAttachmentResponse).collect(Collectors.toList());
    }

    private TicketAttachmentResponse toAttachmentResponse(TicketAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        return new TicketAttachmentResponse(
                attachment.getAttachmentId(),
                attachment.getFilename(),
                attachment.getS3Url(),
                attachment.getFileSize(),
                attachment.getMimeType(),
                attachment.getUploadedBy(),
                attachment.getUploadedAt());
    }

    private List<TicketStatusHistoryResponse> toStatusHistoryResponses(List<TicketStatusHistory> history) {
        if (history == null) {
            return List.of();
        }
        return history.stream().map(this::toStatusHistoryResponse).collect(Collectors.toList());
    }

    private TicketStatusHistoryResponse toStatusHistoryResponse(TicketStatusHistory history) {
        if (history == null) {
            return null;
        }
        return new TicketStatusHistoryResponse(
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedBy(),
                history.getChangedAt(),
                history.getReason());
    }

    private List<TicketSlaEventResponse> toSlaEventResponses(List<TicketSlaEvent> events) {
        if (events == null) {
            return List.of();
        }
        return events.stream().map(this::toSlaEventResponse).collect(Collectors.toList());
    }

    private TicketSlaEventResponse toSlaEventResponse(TicketSlaEvent event) {
        if (event == null) {
            return null;
        }
        return new TicketSlaEventResponse(event.getEventType(), event.getTriggeredAt(), event.getNotifiedRoles());
    }

    private TicketSlaSummary buildSlaSummary(Ticket ticket) {
        if (ticket.getSlaDeadline() == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        long remainingMinutes = Duration.between(now, ticket.getSlaDeadline()).toMinutes();
        if (remainingMinutes < 0) {
            remainingMinutes = 0;
        }
        boolean breached = ticket.getSlaBreachedAt() != null || now.isAfter(ticket.getSlaDeadline());
        return new TicketSlaSummary(ticket.getSlaDeadline(), remainingMinutes, breached);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
