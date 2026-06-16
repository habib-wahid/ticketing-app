package com.example.ticketing_app.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import com.example.ticketing_app.dto.TicketDailyStatusResponse;

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

	private String escapeRegex(String value) {
		return value.replaceAll("([\\\\.+*?\\[\\](){}^$|])", "\\\\$1");
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
	public Page<Ticket> findTicketsDynamic(String createdByUserId, String title, String categoryId, TicketPriority priority,
			List<TicketStatus> statuses, String assignedToUserId, LocalDateTime startDate, LocalDateTime endDate,
			Pageable pageable) {

		List<Criteria> filters = new ArrayList<>();

		if (StringUtils.hasText(createdByUserId)) {
			filters.add(Criteria.where("createdBy.userId").is(createdByUserId));
		}

		if (StringUtils.hasText(title)) {
			// SQL LIKE '%title%' — substring match, case-insensitive
			String term = escapeRegex(title.trim());
			filters.add(Criteria.where("title").regex(term, "i"));
		}

		if (StringUtils.hasText(categoryId) && !"all".equalsIgnoreCase(categoryId)) {
			filters.add(new Criteria().orOperator(
					Criteria.where("category.id").is(categoryId),
					Criteria.where("category._id").is(categoryId)));
		}
		if (priority != null) {
			filters.add(Criteria.where("priority").is(priority));
		}
		if (statuses != null && !statuses.isEmpty()) {
			filters.add(Criteria.where("status").in(statuses));
		}
		if (StringUtils.hasText(assignedToUserId)) {
			filters.add(Criteria.where("assignedTo.userId").is(assignedToUserId));
		}

		if (startDate != null || endDate != null) {
			Criteria dateCriteria = Criteria.where("createdAt");
			if (startDate != null) {
				dateCriteria = dateCriteria.gte(startDate);
			}
			if (endDate != null) {
				dateCriteria = dateCriteria.lte(endDate);
			}
			filters.add(dateCriteria);
		}

		Criteria criteria = filters.isEmpty()
				? new Criteria()
				: new Criteria().andOperator(filters.toArray(Criteria[]::new));

		Query query = new Query(criteria);
		query.fields().exclude("comments").exclude("attachments").exclude("statusHistory").exclude("slaEvents");
		
		long total = mongoTemplate.count(query, Ticket.class);
		
		query.with(pageable);
		List<Ticket> tickets = mongoTemplate.find(query, Ticket.class);

		return new PageImpl<>(tickets, pageable, total);
	}

	@Override
	public Page<Ticket> findAssignedTicketsDynamic(String assignedToUserId, String title, String categoryId,
			TicketPriority priority, List<TicketStatus> statuses, String createdByUserId, LocalDateTime startDate,
			LocalDateTime endDate, Pageable pageable) {

		List<Criteria> filters = new ArrayList<>();

		if (StringUtils.hasText(assignedToUserId)) {
			filters.add(Criteria.where("assignedTo.userId").is(assignedToUserId));
		}

		if (StringUtils.hasText(title)) {
			String term = escapeRegex(title.trim());
			filters.add(Criteria.where("title").regex(term, "i"));
		}

		if (StringUtils.hasText(categoryId) && !"all".equalsIgnoreCase(categoryId)) {
			filters.add(new Criteria().orOperator(
					Criteria.where("category.id").is(categoryId),
					Criteria.where("category._id").is(categoryId)));
		}
		if (priority != null) {
			filters.add(Criteria.where("priority").is(priority));
		}
		if (statuses != null && !statuses.isEmpty()) {
			filters.add(Criteria.where("status").in(statuses));
		}
		if (StringUtils.hasText(createdByUserId)) {
			filters.add(Criteria.where("createdBy.userId").is(createdByUserId));
		}

		if (startDate != null || endDate != null) {
			Criteria dateCriteria = Criteria.where("createdAt");
			if (startDate != null) {
				dateCriteria = dateCriteria.gte(startDate);
			}
			if (endDate != null) {
				dateCriteria = dateCriteria.lte(endDate);
			}
			filters.add(dateCriteria);
		}

		Criteria criteria = filters.isEmpty()
				? new Criteria()
				: new Criteria().andOperator(filters.toArray(Criteria[]::new));

		Query query = new Query(criteria);
		query.fields().exclude("comments").exclude("attachments").exclude("statusHistory").exclude("slaEvents");

		long total = mongoTemplate.count(query, Ticket.class);

		query.with(pageable);
		List<Ticket> tickets = mongoTemplate.find(query, Ticket.class);

		return new PageImpl<>(tickets, pageable, total);
	}

	@Override
	public List<TicketDailyStatusResponse> getDailyTicketStats(LocalDateTime from, LocalDateTime to) {
		Aggregation reportedAggregation = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("createdAt").gte(from).lte(to)),
				Aggregation.project()
						.and("createdAt").dateAsFormattedString("%Y-%m-%d").as("date"),
				Aggregation.group("date").count().as("count"),
				Aggregation.project("count").and("_id").as("date")
		);

		Aggregation solvedAggregation = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("resolvedAt").gte(from).lte(to)),
				Aggregation.project()
						.and("resolvedAt").dateAsFormattedString("%Y-%m-%d").as("date"),
				Aggregation.group("date").count().as("count"),
				Aggregation.project("count").and("_id").as("date")
		);

		List<TicketDailyCountAggregate> reportedRaw = mongoTemplate.aggregate(reportedAggregation,
				mongoTemplate.getCollectionName(Ticket.class), TicketDailyCountAggregate.class).getMappedResults();
		List<TicketDailyCountAggregate> solvedRaw = mongoTemplate.aggregate(solvedAggregation,
				mongoTemplate.getCollectionName(Ticket.class), TicketDailyCountAggregate.class).getMappedResults();

		Map<String, Long> reportedMap = reportedRaw.stream()
				.collect(Collectors.toMap(TicketDailyCountAggregate::getDate, TicketDailyCountAggregate::getCount));
		Map<String, Long> solvedMap = solvedRaw.stream()
				.collect(Collectors.toMap(TicketDailyCountAggregate::getDate, TicketDailyCountAggregate::getCount));

		Map<String, TicketDailyStatusResponse> combined = new TreeMap<>();
		reportedMap.forEach((date, count) -> combined.put(date, new TicketDailyStatusResponse(date, count, 0L)));
		solvedMap.forEach((date, count) -> {
			TicketDailyStatusResponse existing = combined.get(date);
			if (existing != null) {
				combined.put(date, new TicketDailyStatusResponse(date, existing.reportedCount(), count));
			} else {
				combined.put(date, new TicketDailyStatusResponse(date, 0L, count));
			}
		});

		return combined.values().stream().collect(Collectors.toList());
	}
}
