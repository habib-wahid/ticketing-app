package com.example.ticketing_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;

public interface TicketRepository extends MongoRepository<Ticket, String>, TicketCustomRepository {

	Optional<Ticket> findByTicketId(String ticketId);

	@Query(value = "{}", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	List<Ticket> findAllSummary();

	@Query(value = "{}", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	Page<Ticket> findAllSummary(Pageable pageable);

	@Query(value = "{ 'createdBy.userId': ?0 }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	List<Ticket> findByCreatedByUserIdOrderByCreatedAtDesc(String createdByUserId);

	@Query(value = "{ 'createdBy.userId': ?0, 'status': { $in: ?1 } }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	List<Ticket> findByCreatedByUserIdAndStatusInOrderByCreatedAtDesc(String createdByUserId, List<TicketStatus> statuses);

	@Query(value = "{ 'createdBy.userId': ?0 }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }")
	Page<Ticket> findByCreatedByUserId(String createdByUserId, Pageable pageable);

	@Query(value = "{ 'createdBy.userId': ?0, 'status': { $in: ?1 } }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }")
	Page<Ticket> findByCreatedByUserIdAndStatusIn(String createdByUserId, List<TicketStatus> statuses, Pageable pageable);

    List<Ticket> findAllByCreatedByUserId(String createdByUserId);

	@Query(value = "{}", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }")
	List<Ticket> findByCreatedByUserId(String createdByUserId);

	boolean existsByCreatedByUserIdAndTitleIgnoreCaseAndCreatedAtAfter(String createdByUserId, String title,
			LocalDateTime createdAt);

	boolean existsByTicketId(String ticketId);

	@Query(value = "{ 'assignedTo.userId': ?0 }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	List<Ticket> findByAssignedToUserIdOrderByCreatedAtDesc(String assignedToUserId);

	@Query(value = "{ 'assignedTo.userId': ?0, 'status': { $in: ?1 } }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	List<Ticket> findByAssignedToUserIdAndStatusInOrderByCreatedAtDesc(String assignedToUserId, List<TicketStatus> statuses);

	@Query(value = "{ 'assignedTo.userId': ?0 }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }")
	Page<Ticket> findByAssignedToUserId(String assignedToUserId, Pageable pageable);

	@Query(value = "{ 'assignedTo.userId': ?0, 'status': { $in: ?1 } }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }")
	Page<Ticket> findByAssignedToUserIdAndStatusIn(String assignedToUserId, List<TicketStatus> statuses, Pageable pageable);

	long countByStatus(TicketStatus status);

	long countByStatusIn(List<TicketStatus> statuses);

	long countByStatusAndCreatedAtBetween(TicketStatus status, LocalDateTime from, LocalDateTime to);

	long countByStatusAndCreatedAtGreaterThanEqual(TicketStatus status, LocalDateTime from);

	long countByStatusAndCreatedAtLessThanEqual(TicketStatus status, LocalDateTime to);

	long countByStatusInAndCreatedAtBetween(List<TicketStatus> statuses, LocalDateTime from, LocalDateTime to);

	long countByStatusInAndCreatedAtGreaterThanEqual(List<TicketStatus> statuses, LocalDateTime from);

	long countByStatusInAndCreatedAtLessThanEqual(List<TicketStatus> statuses, LocalDateTime to);

	long countByPriority(TicketPriority priority);

	long countByPriorityAndCreatedAtBetween(TicketPriority priority, LocalDateTime from, LocalDateTime to);

	long countByPriorityAndCreatedAtGreaterThanEqual(TicketPriority priority, LocalDateTime from);

	long countByPriorityAndCreatedAtLessThanEqual(TicketPriority priority, LocalDateTime to);

	long countByCreatedByUserIdAndStatus(String createdByUserId, TicketStatus status);

	long countByCreatedByUserIdAndStatusIn(String createdByUserId, List<TicketStatus> statuses);
}
