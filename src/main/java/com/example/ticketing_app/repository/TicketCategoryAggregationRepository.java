package com.example.ticketing_app.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketCategoryAggregationRepository {
	List<TicketCategoryCountProjection> countByCategory();

	List<TicketCategoryCountProjection> countByCategoryCreatedAtBetween(LocalDateTime from, LocalDateTime to);

	List<TicketCategoryCountProjection> countByCategoryCreatedAtGreaterThanEqual(LocalDateTime from);

	List<TicketCategoryCountProjection> countByCategoryCreatedAtLessThanEqual(LocalDateTime to);
}

