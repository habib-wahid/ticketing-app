package com.example.ticketing_app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;
import com.example.ticketing_app.dto.TicketDailyStatusResponse;

public interface TicketCustomRepository {
	List<TicketCategoryCountProjection> countByCategory();

	List<TicketCategoryCountProjection> countByCategoryCreatedAtBetween(LocalDateTime from, LocalDateTime to);

	List<TicketCategoryCountProjection> countByCategoryCreatedAtGreaterThanEqual(LocalDateTime from);

	List<TicketCategoryCountProjection> countByCategoryCreatedAtLessThanEqual(LocalDateTime to);

	Page<Ticket> findTicketsDynamic(String createdByUserId, String categoryId, TicketPriority priority,
			List<TicketStatus> statuses, String assignedToUserId, LocalDateTime startDate, LocalDateTime endDate,
			Pageable pageable);

	List<TicketDailyStatusResponse> getDailyTicketStats(LocalDateTime from, LocalDateTime to);
}
