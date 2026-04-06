package com.example.ticketing_app.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketCommentDeleteRequest(@NotBlank String deletedByUserId) {
}

