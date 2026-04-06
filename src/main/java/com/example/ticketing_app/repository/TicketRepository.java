package com.example.ticketing_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.ticketing_app.entity.Ticket;

public interface TicketRepository extends MongoRepository<Ticket, String> {

	Optional<Ticket> findByTicketId(String ticketId);

	List<Ticket> findByCreatedByUserId(String createdByUserId);

	boolean existsByCreatedByUserIdAndTitleIgnoreCaseAndCreatedAtAfter(String createdByUserId, String title,
			LocalDateTime createdAt);

	boolean existsByTicketId(String ticketId);
}

