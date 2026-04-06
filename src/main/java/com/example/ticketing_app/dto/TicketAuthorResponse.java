package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.UserRole;

public record TicketAuthorResponse(String userId, String fullName, UserRole role) {
}

