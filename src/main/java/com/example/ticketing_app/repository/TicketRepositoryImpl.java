package com.example.ticketing_app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;

@Repository
public class TicketRepositoryImpl implements TicketCustomRepository {

	private final MongoTemplate mongoTemplate;

	public TicketRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public List<TicketCategoryCountProjection> countByCategory() {
		Criteria criteria = Criteria.where("category").ne(null);
		return aggregateByCategory(criteria);
	}

	@Override
	public List<TicketCategoryCountProjection> countByCategoryCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
		Criteria criteria = Criteria.where("category").ne(null)
				.and("createdAt").gte(from).lte(to);
		return aggregateByCategory(criteria);
	}

	@Override
	public List<TicketCategoryCountProjection> countByCategoryCreatedAtGreaterThanEqual(LocalDateTime from) {
		Criteria criteria = Criteria.where("category").ne(null)
				.and("createdAt").gte(from);
		return aggregateByCategory(criteria);
	}

	@Override
	public List<TicketCategoryCountProjection> countByCategoryCreatedAtLessThanEqual(LocalDateTime to) {
		Criteria criteria = Criteria.where("category").ne(null)
				.and("createdAt").lte(to);
		return aggregateByCategory(criteria);
	}

	private List<TicketCategoryCountProjection> aggregateByCategory(Criteria criteria) {
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(criteria),
				Aggregation.project()
						.and(ConditionalOperators.ifNull("category.id").thenValueOf("category._id"))
						.as("categoryId")
						.and("category.name").as("categoryName"),
				Aggregation.group("categoryId", "categoryName").count().as("count"),
				Aggregation.project("count")
						.and("_id.categoryId").as("categoryId")
						.and("_id.categoryName").as("categoryName"),
				Aggregation.sort(Sort.by(Sort.Order.desc("count"), Sort.Order.asc("categoryName")))
		);

		AggregationResults<TicketCategoryCountAggregate> results = mongoTemplate.aggregate(
				aggregation,
				mongoTemplate.getCollectionName(Ticket.class),
				TicketCategoryCountAggregate.class);
		return List.copyOf(results.getMappedResults());
	}

	@Override
	public Page<Ticket> findTicketsDynamic(String createdByUserId, String categoryId, TicketPriority priority,
			List<TicketStatus> statuses, String assignedToUserId, LocalDateTime startDate, LocalDateTime endDate,
			Pageable pageable) {

		Criteria criteria = new Criteria();

		if (StringUtils.hasText(createdByUserId)) {
			criteria.and("createdBy.userId").is(createdByUserId);
		}
		if (StringUtils.hasText(categoryId)) {
			criteria.orOperator(
					Criteria.where("category.id").is(categoryId),
					Criteria.where("category._id").is(categoryId)
			);
		}
		if (priority != null) {
			criteria.and("priority").is(priority);
		}
		if (statuses != null && !statuses.isEmpty()) {
			criteria.and("status").in(statuses);
		}
		if (StringUtils.hasText(assignedToUserId)) {
			criteria.and("assignedTo.userId").is(assignedToUserId);
		}
		if (startDate != null || endDate != null) {
			Criteria dateCriteria = Criteria.where("createdAt");
			if (startDate != null) {
				dateCriteria.gte(startDate);
			}
			if (endDate != null) {
				dateCriteria.lte(endDate);
			}
			criteria.andOperator(dateCriteria);
		}

		Query query = new Query(criteria);
		query.fields().exclude("comments").exclude("attachments").exclude("statusHistory").exclude("slaEvents");
		
		long total = mongoTemplate.count(query, Ticket.class);
		
		query.with(pageable);
		List<Ticket> tickets = mongoTemplate.find(query, Ticket.class);

		return new PageImpl<>(tickets, pageable, total);
	}
}

