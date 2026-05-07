package com.example.ticketing_app.repository;

public record TicketCategoryCountAggregate(
		String categoryId,
		String categoryName,
		long count) implements TicketCategoryCountProjection {

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public String getCategoryName() {
		return categoryName;
	}

	@Override
	public long getCount() {
		return count;
	}
}
