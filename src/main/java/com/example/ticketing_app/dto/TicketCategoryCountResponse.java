package com.example.ticketing_app.dto;

public record TicketCategoryCountResponse(
		String categoryId,
		String categoryName,
		long count) {
}

