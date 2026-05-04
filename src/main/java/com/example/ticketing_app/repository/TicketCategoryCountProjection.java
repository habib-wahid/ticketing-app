package com.example.ticketing_app.repository;

public interface TicketCategoryCountProjection {
	String getCategoryId();

	String getCategoryName();

	long getCount();
}

