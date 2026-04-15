package com.example.ticketing_app.dto;

import com.example.ticketing_app.entity.TicketStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketStatusChangeRequest(
        @NotNull(message = "Status is required")
        TicketStatus status,
        
        @NotBlank(message = "changedByUserId is required")
        String changedByUserId,
        
        String reason
) {
}

