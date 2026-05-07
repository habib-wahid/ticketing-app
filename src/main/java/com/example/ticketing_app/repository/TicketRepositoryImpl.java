package com.example.ticketing_app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.example.ticketing_app.entity.Ticket;

@Repository
public class TicketRepositoryImpl implements TicketCategoryAggregationRepository {

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
}

