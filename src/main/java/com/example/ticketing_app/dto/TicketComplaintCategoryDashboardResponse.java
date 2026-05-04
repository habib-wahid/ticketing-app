package com.example.ticketing_app.dto;

import java.util.List;

public record TicketComplaintCategoryDashboardResponse(
		List<TicketCategoryCountResponse> categories) {
}

