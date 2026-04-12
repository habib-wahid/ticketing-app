package com.example.ticketing_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketStatus;

public interface TicketRepository extends MongoRepository<Ticket, String> {

	Optional<Ticket> findByTicketId(String ticketId);

	@Query(value = "{}", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	List<Ticket> findAllSummary();

	@Query(value = "{ 'createdByUserId': ?0 }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	List<Ticket> findByCreatedByUserIdOrderByCreatedAtDesc(String createdByUserId);

	@Query(value = "{ 'createdByUserId': ?0, 'status': { $in: ?1 } }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }", sort = "{ 'createdAt': -1 }")
	List<Ticket> findByCreatedByUserIdAndStatusInOrderByCreatedAtDesc(String createdByUserId, List<TicketStatus> statuses);

	@Query(value = "{ 'createdByUserId': ?0 }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }")
	Page<Ticket> findByCreatedByUserId(String createdByUserId, Pageable pageable);

	@Query(value = "{ 'createdByUserId': ?0, 'status': { $in: ?1 } }", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }")
	Page<Ticket> findByCreatedByUserIdAndStatusIn(String createdByUserId, List<TicketStatus> statuses, Pageable pageable);

    List<Ticket> findAllByCreatedByUserId(String createdByUserId);

    @Query(value = "{}", fields = "{ 'comments': 0, 'attachments': 0, 'statusHistory': 0, 'slaEvents': 0 }")
	List<Ticket> findByCreatedByUserId(String createdByUserId);

	boolean existsByCreatedByUserIdAndTitleIgnoreCaseAndCreatedAtAfter(String createdByUserId, String title,
			LocalDateTime createdAt);

	boolean existsByTicketId(String ticketId);
}
