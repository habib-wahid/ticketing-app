package com.example.ticketing_app.service;

import com.example.ticketing_app.dto.CommentAuthorResponse;
import com.example.ticketing_app.dto.ComplaintCategorySummaryResponse;
import com.example.ticketing_app.dto.TicketAssignRequest;
import com.example.ticketing_app.dto.TicketAssignedToResponse;
import com.example.ticketing_app.dto.TicketAssignmentHistoryResponse;
import com.example.ticketing_app.dto.TicketAttachmentResponse;
import com.example.ticketing_app.dto.TicketCategoryCountResponse;
import com.example.ticketing_app.dto.TicketCommentCreateRequest;
import com.example.ticketing_app.dto.TicketCommentDeleteRequest;
import com.example.ticketing_app.dto.TicketCommentResponse;
import com.example.ticketing_app.dto.TicketCommentUpdateRequest;
import com.example.ticketing_app.dto.TicketCreateRequest;
import com.example.ticketing_app.dto.TicketCreatedByResponse;
import com.example.ticketing_app.dto.TicketDailyStatusResponse;
import com.example.ticketing_app.dto.TicketDashboardResponse;
import com.example.ticketing_app.dto.TicketDistributedByResponse;
import com.example.ticketing_app.dto.TicketPriorityDashboardResponse;
import com.example.ticketing_app.dto.TicketResponse;
import com.example.ticketing_app.dto.TicketReturnRequest;
import com.example.ticketing_app.dto.AssignedTicketSearchRequest;
import com.example.ticketing_app.dto.TicketSearchRequest;
import com.example.ticketing_app.dto.TicketSlaEventResponse;
import com.example.ticketing_app.dto.TicketSlaSummary;
import com.example.ticketing_app.dto.TicketStatusChangeRequest;
import com.example.ticketing_app.dto.TicketStatusHistoryResponse;
import com.example.ticketing_app.dto.TicketSummaryResponse;
import com.example.ticketing_app.dto.TicketUpdateRequest;
import com.example.ticketing_app.dto.UserTicketStatsResponse;
import com.example.ticketing_app.entity.CommentAuthor;
import com.example.ticketing_app.entity.ComplaintCategory;
import com.example.ticketing_app.entity.SlaPolicy;
import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketAssignedTo;
import com.example.ticketing_app.entity.TicketAssignmentAction;
import com.example.ticketing_app.entity.TicketAssignmentHistory;
import com.example.ticketing_app.entity.TicketAttachment;
import com.example.ticketing_app.entity.TicketComment;
import com.example.ticketing_app.entity.TicketCreatedBy;
import com.example.ticketing_app.entity.TicketDistributedBy;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketSlaEvent;
import com.example.ticketing_app.entity.TicketSlaEventType;
import com.example.ticketing_app.entity.TicketStatus;
import com.example.ticketing_app.entity.TicketStatusHistory;
import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.entity.UserRole;
import com.example.ticketing_app.exception.BadRequestException;
import com.example.ticketing_app.exception.ForbiddenException;
import com.example.ticketing_app.exception.ResourceNotFoundException;
import com.example.ticketing_app.repository.ComplaintCategoryRepository;
import com.example.ticketing_app.repository.TicketCategoryCountProjection;
import com.example.ticketing_app.repository.TicketRepository;
import com.example.ticketing_app.repository.UserRepository;
import com.example.ticketing_app.service.sla.SlaClock;
import com.example.ticketing_app.service.sla.SlaClockTargets;
import com.example.ticketing_app.service.sla.SlaDeadlines;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketService {

    private static final int MAX_TAGS = 5;
    private static final String SYSTEM_ACTOR = "system";

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SlaPolicyService slaPolicyService;
    private final ComplaintCategoryRepository complaintCategoryRepository;
    private final CategoryDistributorMappingService categoryDistributorMappingService;
    private final FileStorageService fileStorageService;
    private final SlaClock slaClock;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, SlaPolicyService slaPolicyService,
            ComplaintCategoryRepository complaintCategoryRepository,
            CategoryDistributorMappingService categoryDistributorMappingService,
            FileStorageService fileStorageService, SlaClock slaClock) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.slaPolicyService = slaPolicyService;
        this.complaintCategoryRepository = complaintCategoryRepository;
        this.categoryDistributorMappingService = categoryDistributorMappingService;
        this.fileStorageService = fileStorageService;
        this.slaClock = slaClock;
    }

    public Page<TicketSummaryResponse> findAll(ActorContext actor, Pageable pageable) {
        log.info("Finding tickets for actor {}", actor);
        Page<Ticket> page = actor.isAdmin()
                ? ticketRepository.findAllSummary(pageable)

                : ticketRepository.findByCreatedByUserId(actor.userId(), pageable);
        log.info("Found total {} tickets", page.getTotalElements());
        return toSummaryPage(page);
    }

    public Page<TicketSummaryResponse> findByCreatedByUserId(String userId, TicketStatus status, Pageable pageable) {
        Page<Ticket> page = status == null ? ticketRepository.findByCreatedByUserId(userId, pageable)
                : ticketRepository.findByCreatedByUserIdAndStatusIn(userId, List.of(status), pageable);
        return toSummaryPage(page);
    }

    public Page<TicketSummaryResponse> findMyTickets(String actorUserId, String title, TicketSearchRequest request,
            Pageable pageable) {
		List<TicketStatus> statuses = request.status() != null ? List.of(request.status()) : null;
		String titleFilter = StringUtils.hasText(title) ? title : request.title();

		Page<Ticket> page = ticketRepository.findTicketsDynamic(actorUserId, titleFilter, request.categoryId(),
				request.priority(), statuses, request.assignedTo(), request.startDate(), request.endDate(), pageable);

		return toSummaryPage(page);
	}

    public Page<TicketSummaryResponse> findMyAssignedTickets(String actorUserId, String title,
            AssignedTicketSearchRequest request, Pageable pageable) {
        List<TicketStatus> statuses = request.status() != null ? List.of(request.status()) : null;
        String titleFilter = StringUtils.hasText(title) ? title : request.title();

        Page<Ticket> page = ticketRepository.findAssignedTicketsDynamic(actorUserId, titleFilter, request.categoryId(),
                request.priority(), statuses, request.createdBy(), request.startDate(), request.endDate(), pageable);

        return toSummaryPage(page);
    }

	private Map<String, String> loadAssignedUserNames(List<Ticket> tickets) {
		return Collections.emptyMap();
	}

    public UserTicketStatsResponse getMyTicketStats(String userId) {
        long totalTickets = ticketRepository.countByCreatedByUserId(userId);
        long assignedTickets = ticketRepository.countByAssignedToUserId(userId);
        long resolvedTickets = ticketRepository.countByCreatedByUserIdAndStatus(userId, TicketStatus.RESOLVED);
        return new UserTicketStatsResponse(totalTickets, assignedTickets, resolvedTickets);
    }

    public TicketResponse findByTicketId(String ticketId, ActorContext actor) {
        return toResponse(getTicketEntity(ticketId, actor));
    }

    public TicketResponse create(TicketCreateRequest request, ActorContext actor, List<MultipartFile> files) {
        String createdByUserId = actor.isAdmin() && StringUtils.hasText(request.createdByUserId())
                ? request.createdByUserId().trim()
                : actor.userId();
        User createdBy = userRepository.findByUserId(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Created-by user not found: " + createdByUserId));

        ComplaintCategory complaintCategory = resolveComplaintCategory(request.complaintCategoryId());
        User distributor = categoryDistributorMappingService.requireActiveDistributor(complaintCategory.getId());

        String normalizedTitle = request.title().trim();
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setTicketId(generateTicketId());
        ticket.setTitle(normalizedTitle);
        ticket.setDescription(request.description().trim());
        ticket.setCategory(complaintCategory);
        ticket.setPriority(request.priority());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedBy(new TicketCreatedBy(buildFullName(createdBy), createdBy.getUserId(), createdBy.getRole()));

        changeAssignee(ticket, distributor, null, TicketAssignmentAction.AUTO_ROUTED,
                "Auto-routed to category distributor", now);

        applySlaPolicy(ticket, ticket.getPriority(), now);
        ticket.setTags(normalizeTags(request.tags()));
        ticket.setCustomFields(request.customFields() == null ? new HashMap<>() : new HashMap<>(request.customFields()));
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    ticket.getAttachments().add(buildAttachment(ticket.getTicketId(), file, createdBy.getUserId(), now));
                }
            }
        }

        return toResponse(ticketRepository.save(ticket));
    }

    public TicketResponse update(String ticketId, TicketUpdateRequest request, ActorContext actor) {
        return update(ticketId, request, actor, null);
    }

    public TicketResponse update(String ticketId, TicketUpdateRequest request, ActorContext actor, List<MultipartFile> files) {
        Ticket ticket = getTicketEntity(ticketId, actor);
        if (actor.isCustomer() && (request.assignedToUserId() != null || request.status() != null)) {
            throw new ForbiddenException("Only admins can change assignee or status");
        }
        LocalDateTime now = LocalDateTime.now();

        if (StringUtils.hasText(request.title())) {
            ticket.setTitle(request.title().trim());
        }
        if (StringUtils.hasText(request.description())) {
            ticket.setDescription(request.description().trim());
        }
        if (request.complaintCategoryId() != null) {
            String complaintCategoryId = normalize(request.complaintCategoryId());
            if (!StringUtils.hasText(complaintCategoryId)) {
                throw new BadRequestException("Complaint category id cannot be blank");
            }
            ticket.setCategory(resolveComplaintCategory(complaintCategoryId));
            applySlaPolicy(ticket, ticket.getPriority(), ticket.getCreatedAt() == null ? now : ticket.getCreatedAt());
        }
        if (request.priority() != null && request.priority() != ticket.getPriority()) {
            ticket.setPriority(request.priority());
            applySlaPolicy(ticket, request.priority(), ticket.getCreatedAt() == null ? now : ticket.getCreatedAt());
        }

        String assignedToUserId = request.assignedToUserId() == null ? null : normalize(request.assignedToUserId());
        if (request.assignedToUserId() != null) {
            if (!StringUtils.hasText(assignedToUserId)) {
                throw new BadRequestException("Tickets cannot be unassigned");
            }
            User assignee = userRepository.findByUserId(assignedToUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + assignedToUserId));
            if (assignee.getRole() == UserRole.CUSTOMER) {
                throw new BadRequestException("Tickets can only be assigned to agents, distributors, or admins");
            }
            if (!assignee.isActive()) {
                throw new BadRequestException("Assigned user must be active");
            }
            User actedBy = userRepository.findByUserId(actor.userId()).orElse(null);
            changeAssignee(ticket, assignee, actedBy, TicketAssignmentAction.REASSIGNED, "Reassigned", now);
            recordFirstResponseIfNeeded(ticket, now);
            if (ticket.getStatus() == TicketStatus.NEW) {
                addStatusHistory(ticket, TicketStatus.NEW, TicketStatus.ASSIGNED, SYSTEM_ACTOR, "System", "Assigned");
                ticket.setStatus(TicketStatus.ASSIGNED);
            }
        }

        if (request.status() != null) {
            TicketStatus targetStatus = request.status();
            TicketStatus previousStatus = ticket.getStatus();
            if (targetStatus == TicketStatus.WAITING_ON_CUSTOMER
                    && previousStatus != TicketStatus.WAITING_ON_CUSTOMER) {
                slaClock.onPause(ticket, now);
            } else if (previousStatus == TicketStatus.WAITING_ON_CUSTOMER
                    && targetStatus != TicketStatus.WAITING_ON_CUSTOMER) {
                slaClock.onResume(ticket, now);
            }
            ticket.setStatus(targetStatus);
            applyStatusTimestamps(ticket, targetStatus, now);
            addStatusHistory(ticket, previousStatus, targetStatus, SYSTEM_ACTOR, "System", "Status update");
        }
        applySlaState(ticket, now);
        if (request.tags() != null) {
            ticket.setTags(normalizeTags(request.tags()));
        }
        if (request.customFields() != null) {
            ticket.setCustomFields(new HashMap<>(request.customFields()));
        }

        if (request.removeAttachmentIds() != null && !request.removeAttachmentIds().isEmpty()) {
            Set<String> toRemove = new HashSet<>(request.removeAttachmentIds());
            ticket.getAttachments().removeIf(att -> {
                if (toRemove.contains(att.getAttachmentId())) {
                    fileStorageService.deleteTicketAttachment(ticketId, extractStoredFilename(att));
                    return true;
                }
                return false;
            });
        }

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    ticket.getAttachments().add(buildAttachment(ticketId, file, actor.userId(), now));
                }
            }
        }

        ticket.setUpdatedAt(now);
        return toResponse(ticketRepository.save(ticket));
    }

    private String extractStoredFilename(TicketAttachment att) {
        String path = att.getFilePath() != null ? att.getFilePath() : att.getS3Url();
        if (path == null) {
            return null;
        }
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
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
        comment.setText(request.content().trim());
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

    public void delete(String ticketId, ActorContext actor) {
        Ticket ticket = getTicketEntity(ticketId, actor);
        ensureTicketInDeletableState(ticket);
        ticketRepository.delete(ticket);
    }

    public TicketResponse assign(String ticketId, TicketAssignRequest request, ActorContext actorContext) {
        Ticket ticket = getTicketEntity(ticketId, actorContext);
        User actor = userRepository.findByUserId(actorContext.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + actorContext.userId()));

        if (!(actorContext.isAdmin() || actorContext.isDistributor())) {
            throw new ForbiddenException("Only distributors or admins can distribute tickets");
        }
        if (actorContext.isDistributor()
                && (ticket.getAssignedTo() == null || !actorContext.userId().equals(ticket.getAssignedTo().getUserId()))) {
            throw new ForbiddenException("You can only distribute tickets assigned to you");
        }
        if (ticket.getStatus() != TicketStatus.NEW && ticket.getStatus() != TicketStatus.REOPENED) {
            throw new BadRequestException("Ticket can only be distributed from NEW or REOPENED status");
        }

        String assignedToUserId = normalize(request.assignedToUserId());
        User assignee = userRepository.findByUserId(assignedToUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + assignedToUserId));
        if (assignee.getRole() != UserRole.AGENT) {
            throw new BadRequestException("Tickets can only be distributed to agents");
        }
        if (!assignee.isActive()) {
            throw new BadRequestException("Assigned user must be active");
        }

        TicketStatus previousStatus = ticket.getStatus();
        TicketStatus requestedStatus = TicketStatus.ASSIGNED;
        ticket.setStatus(requestedStatus);

        LocalDateTime now = LocalDateTime.now();
        String reason = StringUtils.hasText(request.reason()) ? request.reason().trim() : "Distributed to agent";
        changeAssignee(ticket, assignee, actor, TicketAssignmentAction.DISTRIBUTED, reason, now);
        ticket.setDistributedBy(new TicketDistributedBy(actor.getUserId(), buildFullName(actor), actor.getRole()));
        recordFirstResponseIfNeeded(ticket, now);
        ticket.setUpdatedAt(now);
        addStatusHistory(ticket, previousStatus, requestedStatus, actor.getUserId(),
                buildFullName(actor), reason);
        applySlaState(ticket, now);
        return toResponse(ticketRepository.save(ticket));
    }

    public TicketResponse returnToDistributor(String ticketId, TicketReturnRequest request, ActorContext actorContext) {
        Ticket ticket = getTicketEntity(ticketId, actorContext);
        User actor = userRepository.findByUserId(actorContext.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + actorContext.userId()));

        if (actor.getRole() != UserRole.AGENT) {
            throw new ForbiddenException("Only the assigned agent can return a ticket to the distributor");
        }
        if (ticket.getAssignedTo() == null || !actorContext.userId().equals(ticket.getAssignedTo().getUserId())) {
            throw new ForbiddenException("You can only return tickets assigned to you");
        }
        if (ticket.getDistributedBy() == null || !StringUtils.hasText(ticket.getDistributedBy().getUserId())) {
            throw new BadRequestException("No distributor recorded for this ticket");
        }

        User distributor = userRepository.findByUserId(ticket.getDistributedBy().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Distributor not found: " + ticket.getDistributedBy().getUserId()));
        if (distributor.getRole() != UserRole.DISTRIBUTOR || !distributor.isActive()) {
            throw new BadRequestException("Original distributor is not available");
        }

        TicketStatus previousStatus = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now();
        String reason = request != null && StringUtils.hasText(request.reason())
                ? request.reason().trim()
                : "Returned to distributor";

        changeAssignee(ticket, distributor, actor, TicketAssignmentAction.RETURNED_TO_DISTRIBUTOR, reason, now);
        ticket.setDistributedBy(null);
        ticket.setStatus(TicketStatus.NEW);
        addStatusHistory(ticket, previousStatus, TicketStatus.NEW, actor.getUserId(), buildFullName(actor), reason);
        ticket.setUpdatedAt(now);
        applySlaState(ticket, now);
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
                buildFullName(actor),
                StringUtils.hasText(request.reason()) ? request.reason().trim() : "Status changed");
        applySlaState(ticket, now);
        ticket.setUpdatedAt(now);
        return toResponse(ticketRepository.save(ticket));
    }

    public TicketDashboardResponse getDashboardCounts(ActorContext actor, LocalDateTime from, LocalDateTime to) {
        validateDateRange(from, to);
        List<TicketStatus> openStatuses = List.of(
                TicketStatus.NEW,
                TicketStatus.ASSIGNED,
                TicketStatus.IN_PROGRESS,
                TicketStatus.REOPENED,
                TicketStatus.RESOLVED);
        List<TicketStatus> inProcessStatuses = List.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS);

        long openTickets = countByStatusIn(openStatuses, from, to);
        long newTickets = countByStatus(TicketStatus.NEW, from, to);
        long inProcessTickets = countByStatusIn(inProcessStatuses, from, to);
        long closedTickets = countByStatus(TicketStatus.CLOSED, from, to);

        return new TicketDashboardResponse(openTickets, newTickets, inProcessTickets, closedTickets);
    }

    public TicketPriorityDashboardResponse getDashboardCountsByPriority(ActorContext actor, LocalDateTime from, LocalDateTime to) {
        validateDateRange(from, to);
        long low = countByPriority(TicketPriority.LOW, from, to);
        long medium = countByPriority(TicketPriority.MEDIUM, from, to);
        long high = countByPriority(TicketPriority.HIGH, from, to);
        long critical = countByPriority(TicketPriority.CRITICAL, from, to);

        return new TicketPriorityDashboardResponse(low, medium, high, critical);
    }

    public List<TicketCategoryCountProjection> getDashboardCountsByComplaintCategory(ActorContext actor,
            LocalDateTime from, LocalDateTime to) {
        validateDateRange(from, to);
        return countByComplaintCategory(from, to);
    }

    private Ticket getTicketEntity(String ticketId) {
        return ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
    }

    private Ticket getTicketEntity(String ticketId, ActorContext actor) {
        Ticket ticket = getTicketEntity(ticketId);
        ensureAccess(ticket, actor);
        return ticket;
    }

    private void ensureTicketInDeletableState(Ticket ticket) {
        if (!(ticket.getStatus() == TicketStatus.ASSIGNED || ticket.getStatus() == TicketStatus.NEW)) {
            throw new BadRequestException(String.format("Ticket can not be deleted from status: %s", ticket.getStatus()));
        }
    }

    private void ensureAccess(Ticket ticket, ActorContext actor) {
        if (actor.isAdmin()) {
            return;
        }

        TicketCreatedBy createdBy = ticket.getCreatedBy();

        if (createdBy == null || actor.userId().equals(createdBy.getUserId())) {
            return;
        }
        if (ticket.getAssignedTo() != null && actor.userId().equals(ticket.getAssignedTo().getUserId())) {
            return;
        }

        throw new ForbiddenException("You can only access your own tickets");
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                toCategorySummary(ticket.getCategory()),
                ticket.getPriority(),
                ticket.getStatus(),
                toCreatedByResponse(ticket),
                toAssignedToResponse(ticket.getAssignedTo()),
                toDistributedByResponse(ticket.getDistributedBy()),
                ticket.getAssignedAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.getSlaPolicyId(),
                buildSlaSummary(ticket),
                ticket.getResponseDeadline(),
                ticket.getEscalationDueAt(),
                ticket.getNextReminderAt(),
                ticket.getSlaBreachedAt(),
                ticket.getEscalationLevel(),
                ticket.getFirstResponseMinutes(),
                ticket.getResponseBreached(),
                toCommentResponses(ticket.getComments()),
                toAttachmentResponses(ticket.getAttachments()),
                toStatusHistoryResponses(ticket.getStatusHistory()),
                toAssignmentHistoryResponses(ticket.getAssignmentHistory()),
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
				toCategorySummary(ticket.getCategory()),
				ticket.getPriority(),
				ticket.getStatus(),
				toCreatedByResponse(ticket),
				toAssignedToResponse(ticket.getAssignedTo()),
				ticket.getAssignedAt(),
				ticket.getResolvedAt(),
				ticket.getClosedAt(),
				ticket.getSlaPolicyId(),
				buildSlaSummary(ticket),
			 ticket.getResponseDeadline(),
			 ticket.getEscalationDueAt(),
			 ticket.getNextReminderAt(),
			 ticket.getSlaBreachedAt(),
			 ticket.getEscalationLevel(),
			 ticket.getFirstResponseMinutes(),
			 ticket.getResponseBreached(),
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

    private String resolvePolicyCategoryId(Ticket ticket) {
        return ticket.getCategory() != null ? ticket.getCategory().getId() : null;
    }

    private void applySlaPolicy(Ticket ticket, TicketPriority priority, LocalDateTime createdAt) {
        SlaPolicy policy = slaPolicyService.findPolicy(resolvePolicyCategoryId(ticket), priority);
        ticket.setSlaPolicyId(policy != null ? policy.getId() : null);
        SlaClockTargets targets = SlaClockTargets.from(policy, priority);
        SlaDeadlines deadlines = slaClock.computeDeadlines(createdAt, targets);

        ticket.setResponseDeadline(deadlines.responseDeadline());
        ticket.setSlaDeadline(deadlines.slaDeadline());
        ticket.setEscalationDueAt(deadlines.escalationDueAt());
        ticket.setNextReminderAt(deadlines.nextReminderAt());
        appendSlaEvent(ticket, TicketSlaEventType.SLA_APPLIED, createdAt);
    }

    private void applySlaState(Ticket ticket, LocalDateTime now) {
        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            if (ticket.getStatus() == TicketStatus.RESOLVED
                    && !hasSlaEvent(ticket, TicketSlaEventType.RESOLUTION_MET)) {
                appendSlaEvent(ticket, TicketSlaEventType.RESOLUTION_MET, now);
            }
            return;
        }
        if (slaClock.isResponseBreachedUnassigned(now, ticket)) {
            if (!Boolean.TRUE.equals(ticket.getResponseBreached())) {
                ticket.setResponseBreached(true);
                appendSlaEventOnce(ticket, TicketSlaEventType.RESPONSE_BREACHED, now);
            }
        }
        if (ticket.getSlaDeadline() != null && ticket.getSlaBreachedAt() == null && slaClock.isBreached(now, ticket)) {
            ticket.setSlaBreachedAt(now);
            appendSlaEventOnce(ticket, TicketSlaEventType.SLA_BREACHED, now);
        }
        if (ticket.getEscalationDueAt() != null && ticket.getEscalationLevel() != null
                && ticket.getEscalationLevel() == 0 && now.isAfter(ticket.getEscalationDueAt())) {
            ticket.setEscalationLevel(1);
            appendSlaEvent(ticket, TicketSlaEventType.ESCALATION_TRIGGERED, now);
        }

        SlaPolicy policy = slaPolicyService.findPolicy(resolvePolicyCategoryId(ticket), ticket.getPriority());
        SlaClockTargets targets = SlaClockTargets.from(policy, ticket.getPriority());
        if (ticket.getNextReminderAt() != null && now.isAfter(ticket.getNextReminderAt())) {
            appendSlaEvent(ticket, TicketSlaEventType.DEADLINE_APPROACHING, now);
            ticket.setNextReminderAt(now.plusHours(targets.reminderThresholdHours()));
        }
    }

    private void recordFirstResponseIfNeeded(Ticket ticket, LocalDateTime assignedAt) {
        if (ticket.getFirstResponseMinutes() != null) {
            return;
        }
        long minutes = slaClock.calculateFirstResponseMinutes(ticket, assignedAt);
        ticket.setFirstResponseMinutes(minutes);
        boolean breached = slaClock.isResponseBreached(assignedAt, ticket.getResponseDeadline());
        ticket.setResponseBreached(breached);
        appendSlaEventOnce(ticket,
                breached ? TicketSlaEventType.RESPONSE_BREACHED : TicketSlaEventType.RESPONSE_MET,
                assignedAt);
    }

    private void appendSlaEventOnce(Ticket ticket, TicketSlaEventType type, LocalDateTime at) {
        if (hasSlaEvent(ticket, type)) {
            return;
        }
        appendSlaEvent(ticket, type, at);
    }

    private boolean hasSlaEvent(Ticket ticket, TicketSlaEventType type) {
        if (ticket.getSlaEvents() == null) {
            return false;
        }
        return ticket.getSlaEvents().stream().anyMatch(e -> e.getEventType() == type);
    }

    private void appendSlaEvent(Ticket ticket, TicketSlaEventType type, LocalDateTime at) {
        TicketSlaEvent event = new TicketSlaEvent();
        event.setEventType(type);
        event.setTriggeredAt(at);
        ticket.getSlaEvents().add(event);
    }

    /**
     * Closes any open assignment tenure and opens a new one for {@code assignee}.
     * Never clears {@code assignedTo}.
     */
    private void changeAssignee(Ticket ticket, User assignee, User actedBy, TicketAssignmentAction action,
            String reason, LocalDateTime now) {
        changeAssignee(ticket, assignee, actedBy, action, reason, now,
                actedBy != null ? actedBy.getUserId() : SYSTEM_ACTOR,
                actedBy != null ? buildFullName(actedBy) : "System");
    }

    private void changeAssignee(Ticket ticket, User assignee, User actedBy, TicketAssignmentAction action,
            String reason, LocalDateTime now, String actedByUserId, String actedByName) {
        if (ticket.getAssignedTo() != null
                && assignee.getUserId().equals(ticket.getAssignedTo().getUserId())) {
            ticket.setAssignedAt(now);
            return;
        }

        closeOpenAssignmentHistory(ticket, now);

        ticket.setAssignedTo(new TicketAssignedTo(buildFullName(assignee), assignee.getUserId(), assignee.getRole()));
        ticket.setAssignedAt(now);

        TicketAssignmentHistory history = new TicketAssignmentHistory();
        history.setUserId(assignee.getUserId());
        history.setName(buildFullName(assignee));
        history.setRole(assignee.getRole());
        history.setFromAt(now);
        history.setAction(action);
        history.setActedByUserId(actedByUserId);
        history.setActedByName(actedByName);
        history.setReason(reason);
        if (action == TicketAssignmentAction.DISTRIBUTED && actedBy != null) {
            history.setDistributedByUserId(actedBy.getUserId());
        } else if (ticket.getDistributedBy() != null) {
            history.setDistributedByUserId(ticket.getDistributedBy().getUserId());
        }
        ticket.getAssignmentHistory().add(history);
    }

    private void closeOpenAssignmentHistory(Ticket ticket, LocalDateTime now) {
        if (ticket.getAssignmentHistory() == null || ticket.getAssignmentHistory().isEmpty()) {
            return;
        }
        for (int i = ticket.getAssignmentHistory().size() - 1; i >= 0; i--) {
            TicketAssignmentHistory open = ticket.getAssignmentHistory().get(i);
            if (open.getToAt() == null) {
                open.setToAt(now);
                LocalDateTime fromAt = open.getFromAt() != null ? open.getFromAt() : now;
                open.setDurationMinutes(Math.max(0, Duration.between(fromAt, now).toMinutes()));
                return;
            }
        }
    }

    private TicketDistributedByResponse toDistributedByResponse(TicketDistributedBy distributedBy) {
        if (distributedBy == null) {
            return null;
        }
        return new TicketDistributedByResponse(distributedBy.getUserId(), distributedBy.getName(), distributedBy.getRole());
    }

    private List<TicketAssignmentHistoryResponse> toAssignmentHistoryResponses(List<TicketAssignmentHistory> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        return history.stream().map(this::toAssignmentHistoryResponse).collect(Collectors.toList());
    }

    private TicketAssignmentHistoryResponse toAssignmentHistoryResponse(TicketAssignmentHistory history) {
        Long duration = history.getDurationMinutes();
        if (duration == null && history.getFromAt() != null && history.getToAt() == null) {
            duration = Math.max(0, Duration.between(history.getFromAt(), LocalDateTime.now()).toMinutes());
        }
        return new TicketAssignmentHistoryResponse(
                history.getUserId(),
                history.getName(),
                history.getRole(),
                history.getFromAt(),
                history.getToAt(),
                duration,
                history.getAction(),
                history.getActedByUserId(),
                history.getActedByName(),
                history.getReason(),
                history.getDistributedByUserId());
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

    private void addStatusHistory(Ticket ticket, TicketStatus from, TicketStatus to, String actor, String name, String reason) {
        TicketStatusHistory history = new TicketStatusHistory();
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(actor);
        history.setName(name);
        history.setChangedAt(LocalDateTime.now());
        history.setReason(reason);
        ticket.getStatusHistory().add(history);
    }

    private void validateCommentAccess(User actor, Ticket ticket) {
        TicketCreatedBy createdBy = ticket.getCreatedBy();
        if (createdBy == null
                || !actor.getUserId().equals(createdBy.getUserId())) {
            throw new BadRequestException("You are not allowed to change this comment");
        }
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from must be before to");
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
                attachment.getFilePath(),
                attachment.getS3Url(),
                attachment.getFileSize(),
                attachment.getMimeType(),
                attachment.getUploadedBy(),
                attachment.getUploadedAt());
    }

    private TicketAttachment buildAttachment(String ticketId, MultipartFile file, String uploadedBy, LocalDateTime uploadedAt) {
        FileStorageService.StoredFile storedFile = fileStorageService.storeTicketAttachment(ticketId, file);
        TicketAttachment attachment = new TicketAttachment();
        attachment.setAttachmentId(generateAttachmentId());
        attachment.setFilename(storedFile.originalFilename());
        attachment.setFilePath(storedFile.accessiblePath());
        attachment.setS3Url(storedFile.accessiblePath());
        attachment.setFileSize(storedFile.fileSize());
        attachment.setMimeType(storedFile.mimeType());
        attachment.setUploadedBy(uploadedBy);
        attachment.setUploadedAt(uploadedAt);
        return attachment;
    }

    private String generateAttachmentId() {
        return "att_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
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
                history.getName(),
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
        return new TicketSlaEventResponse(
                event.getEventType(),
                event.getTriggeredAt(),
                event.getNotifiedRoles());
    }


    private TicketSlaSummary buildSlaSummary(Ticket ticket) {
        if (ticket.getSlaDeadline() == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        long remainingMinutes = slaClock.remainingMinutes(now, ticket);
        boolean breached = slaClock.isBreached(now, ticket) || ticket.getSlaBreachedAt() != null;
        return new TicketSlaSummary(ticket.getSlaDeadline(), remainingMinutes, breached);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Page<TicketSummaryResponse> toSummaryPage(Page<Ticket> page) {
        List<TicketSummaryResponse> responses = page.getContent().stream().map(this::toSummaryResponse).collect(Collectors.toList());
        return new PageImpl<>(responses, page.getPageable(), page.getTotalElements());
    }


    private ComplaintCategory resolveComplaintCategory(String complaintCategoryId) {
        String normalizedId = normalize(complaintCategoryId);
        if (!StringUtils.hasText(normalizedId)) {
            throw new BadRequestException("Complaint category id is required");
        }
        return complaintCategoryRepository.findById(normalizedId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint category not found: " + normalizedId));
    }

    private ComplaintCategorySummaryResponse toCategorySummary(ComplaintCategory category) {
        if (category == null) {
            return null;
        }
        return new ComplaintCategorySummaryResponse(category.getId(), category.getName());
    }

    private long countByStatus(TicketStatus status, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return ticketRepository.countByStatusAndCreatedAtBetween(status, from, to);
        }
        if (from != null) {
            return ticketRepository.countByStatusAndCreatedAtGreaterThanEqual(status, from);
        }
        if (to != null) {
            return ticketRepository.countByStatusAndCreatedAtLessThanEqual(status, to);
        }
        return ticketRepository.countByStatus(status);
    }

    private long countByStatusIn(List<TicketStatus> statuses, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return ticketRepository.countByStatusInAndCreatedAtBetween(statuses, from, to);
        }
        if (from != null) {
            return ticketRepository.countByStatusInAndCreatedAtGreaterThanEqual(statuses, from);
        }
        if (to != null) {
            return ticketRepository.countByStatusInAndCreatedAtLessThanEqual(statuses, to);
        }
        return ticketRepository.countByStatusIn(statuses);
    }

    private long countByPriority(TicketPriority priority, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return ticketRepository.countByPriorityAndCreatedAtBetween(priority, from, to);
        }
        if (from != null) {
            return ticketRepository.countByPriorityAndCreatedAtGreaterThanEqual(priority, from);
        }
        if (to != null) {
            return ticketRepository.countByPriorityAndCreatedAtLessThanEqual(priority, to);
        }
        return ticketRepository.countByPriority(priority);
    }

    private TicketCategoryCountResponse toCategoryCountResponse(TicketCategoryCountProjection projection) {
        String name = projection.getCategoryName();
        if (!StringUtils.hasText(name)) {
            name = projection.getCategoryId();
        }
        return new TicketCategoryCountResponse(projection.getCategoryId(), name, projection.getCount());
    }

    private List<TicketCategoryCountProjection> countByComplaintCategory(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return ticketRepository.countByCategoryCreatedAtBetween(from, to);
        }
        if (from != null) {
            return ticketRepository.countByCategoryCreatedAtGreaterThanEqual(from);
        }
        if (to != null) {
            return ticketRepository.countByCategoryCreatedAtLessThanEqual(to);
        }
        return ticketRepository.countByCategory();
    }

    public List<TicketDailyStatusResponse> getDailyTicketStats(ActorContext actor, LocalDateTime from, LocalDateTime to) {
        validateDateRange(from, to);
        return ticketRepository.getDailyTicketStats(from, to);
    }
}
