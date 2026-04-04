package com.example.ticketing_app.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.ticketing_app.entity.SlaPolicy;
import com.example.ticketing_app.entity.TicketPriority;

public interface SlaPolicyRepository extends MongoRepository<SlaPolicy, String> {

	Optional<SlaPolicy> findByPriority(TicketPriority priority);

	boolean existsByPriority(TicketPriority priority);
}

