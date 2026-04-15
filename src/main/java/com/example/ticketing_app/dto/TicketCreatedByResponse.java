package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.UserRole;

public record TicketCreatedByResponse(String userId, String name, UserRole role) {
}
